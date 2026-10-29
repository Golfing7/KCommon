package com.golfing8.kcommon.util;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.PluginClassLoader;

import java.lang.reflect.*;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;

/**
 * Contains useful reflection utilities.
 */
public final class Reflection extends ReflectionCore {
    private Reflection() {
    }

    /**
     * Discovers all classes that extend module except for {@link Module}.
     *
     * @param loader the class loader to check under.
     * @return the modules.
     */
    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends Module>> discoverModules(URLClassLoader loader) {
        List<Class<? extends Module>> classes = new ArrayList<>();
        for (Class<?> moduleClass : ModuleLoader.discoverModules(loader)) {
            if (Module.class.isAssignableFrom(moduleClass) && Module.class != moduleClass)
                classes.add((Class<? extends Module>) moduleClass);
        }
        return classes;
    }

    /**
     * Uses reflection to detect all present module classes and instantiate them.
     *
     * @param classLoader the plugin classloader to search for
     * @param moduleInitializer the initializer for modules
     */
    public static void reflectivelySetupModules(PluginClassLoader classLoader, Consumer<Module> moduleInitializer) {
        NMSVersion serverVersion = KCommon.getInstance().getServerVersion();
        KPlugin plugin = (KPlugin) classLoader.getPlugin();
        ModuleLoader.setupModules(classLoader, plugin, info -> {
            if (!info.foliaSupported() && FoliaSchedulers.of(KCommon.getInstance()).getFoliaLib().isFolia()) {
                return false;
            }

            // We can filter modules that are missing plugin dependencies at this point.
            for (String depend : info.pluginDependencies()) {
                if (!Bukkit.getPluginManager().isPluginEnabled(depend)) {
                    return false;
                }
            }

            // Check if the module is running on the proper server version.
            if (info.minimumMajorVersion() > 0 && serverVersion.getMajor() < info.minimumMajorVersion()) {
                return false;
            }
            if (info.maximumMajorVersion() > 0 && serverVersion.getMajor() > info.maximumMajorVersion()) {
                return false;
            }
            if (info.minimumMinorVersion() > 0 && serverVersion.getMinor() < info.minimumMajorVersion()) {
                return false;
            }
            return info.maximumMinorVersion() <= 0 || serverVersion.getMinor() <= info.maximumMinorVersion();
        }, module -> moduleInitializer.accept((Module) module), () -> plugin.getServer().getPluginManager().disablePlugin(plugin));
    }
}
