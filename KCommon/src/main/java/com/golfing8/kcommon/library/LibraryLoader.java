package com.golfing8.kcommon.library;

import com.golfing8.kcommon.KCommon;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Loads libraries dynamically. Inspiration taken from <a href="https://github.com/Pyrbu/ZNPCsPlus/blob/2.X/plugin/src/main/java/lol/pyr/znpcsplus/libraries/LibraryLoader.java">zNPCsPlus</a>
 */
// TODO Recursively load 'compile' scope dependencies from libraries. (i.e. load bson when mongodb-driver-core is requested)
public class LibraryLoader {
    private static final ExecutorService LOADER_SERVICE = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("KCommon Library Loader %s").build());

    private final URLClassLoaderAccess loaderAccess;
    private final Path libraryFolderPath;
    private final Set<Path> loadedLibraries = new HashSet<>();
    private final List<Relocation> relocationRules = new ArrayList<>();

    public LibraryLoader(Plugin plugin, Path libraryFolderPath) {
        this.loaderAccess = URLClassLoaderAccess.create((URLClassLoader) plugin.getClass().getClassLoader());
        this.libraryFolderPath = libraryFolderPath;
    }

    private void ensureLibraryFolderExists() {
        if (!Files.exists(libraryFolderPath))  {
            try {
                Files.createDirectories(libraryFolderPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void deleteUnloadedLibraries() {
        if (!Files.exists(libraryFolderPath))
            return;

        try {
            Files.list(libraryFolderPath).forEach(path -> {
                if (loadedLibraries.contains(path))
                    return;

                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException exc) {
            KCommon.getInstance().getLogger().severe("Failed to list library folder directory.");
            exc.printStackTrace();
        }
    }

    public void addRelocation(String pre, String post) {
        relocationRules.add(new Relocation(pre.replace(",", "."), post.replace(",", ".")));
    }

    /**
     * Loads the given library into runtime. Uses net/group/id notation instead of net.group.id notation to avoid
     * problems with the shade plugin.
     *
     * @param groupId the group id.
     * @param artifactId the artifact ID.
     * @param version the version.
     */
    public void loadLibrary(String groupId, String artifactId, String version) {
        loadLibrary(groupId, artifactId, version, "https://repo1.maven.org/maven2");
    }

    public void loadLibrary(String groupId, String artifactId, String version, String repoUrl) {
        ensureLibraryFolderExists();
        loadLibrary(new LibraryDefinition(groupId, artifactId, version, repoUrl));
    }

    /**
     * Loads all libraries.
     *
     * @param libraryDefinitions the library definitions to load.
     */
    public void loadAllLibraries(Collection<LibraryDefinition> libraryDefinitions) {
        if (libraryDefinitions.isEmpty())
            return;
        ensureLibraryFolderExists();

        // If there is only one library, just load it normally.
        if (libraryDefinitions.size() == 1) {
            for (LibraryDefinition definition : libraryDefinitions) {
                loadLibrary(definition);
            }
            return;
        }

        AtomicInteger completed = new AtomicInteger(0);
        for (LibraryDefinition definition : libraryDefinitions) {
            LOADER_SERVICE.execute(() -> {
                try {
                    loadLibrary(definition);
                } finally {
                    synchronized (this) {
                        if (completed.incrementAndGet() == libraryDefinitions.size()) {
                            this.notifyAll();
                        }
                    }
                }
            });
        }

        // Wait until the execution has finished.
        synchronized (this) {
            if (completed.get() != libraryDefinitions.size()) {
                try {
                    this.wait();
                } catch (InterruptedException exc) {
                    throw new RuntimeException(exc);
                }
            }
        }
    }

    private void loadLibrary(LibraryDefinition definition) {
        Path path = getDependencyPath(definition.getGroupID(), definition.getArtifact(), definition.getVersion());
        URL url;
        try {
            url = getDependencyUrl(definition.getGroupID(), definition.getArtifact(), definition.getVersion(), definition.getRepo());
        } catch (MalformedURLException exc) {
            throw new RuntimeException(exc);
        }
        if (!Files.exists(path)) {
            try (InputStream in = url.openStream()) {
                Path temporaryPath = path.getParent().resolve(path.getFileName().toString() + ".temp");
                Files.copy(in, temporaryPath);
                new JarRelocator(temporaryPath.toFile(), path.toFile(), relocationRules).run();
                Files.delete(temporaryPath);
            } catch (IOException e) {
                KCommon.getInstance().getLogger().severe("Failed to download library " + definition.getFormattedName());
                e.printStackTrace();
            }
        }

        synchronized (this) {
            try {
                loaderAccess.addURL(path.toFile().toURI().toURL());
                loadedLibraries.add(path);
            } catch (Exception e) {
                KCommon.getInstance().getLogger().severe("Failed to load library, plugin may not work correctly (" + definition.getFormattedName() + ")");
                e.printStackTrace();
            }
        }
    }

    private Path getDependencyPath(String groupId, String artifactId, String version) {
        return libraryFolderPath.resolve(groupId.replace(".", "-") + "-"
                + artifactId.replace(".", "-") + "-"
                + version.replace(".", "-") + ".jar");
    }

    private static URL getDependencyUrl(String groupId, String artifactId, String version, String repoUrl) throws MalformedURLException {
        return getUrl(groupId, artifactId, version, version, repoUrl);
    }

    @NotNull
    private static URL getUrl(String groupId, String artifactId, String version, String snapshotVersion, String repoUrl) throws MalformedURLException {
        String url = repoUrl.endsWith("/") ? repoUrl : repoUrl + "/";
        url += groupId.replace(".", "/") + "/";
        url += artifactId + "/";
        url += version + "/";
        url += artifactId + "-" + snapshotVersion + ".jar";
        return new URL(url);
    }
}