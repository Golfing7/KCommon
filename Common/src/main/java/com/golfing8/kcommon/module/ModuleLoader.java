package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Finds the modules in a plugin's jar, then creates and enables them in dependency order.
 */
public final class ModuleLoader {
    private ModuleLoader() {
    }

    /**
     * Discovers all classes that extend module except for {@link AbstractModule<?>}.
     *
     * @param loader the class loader to check under.
     * @return the modules.
     */
    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends AbstractModule<?>>> discoverModules(URLClassLoader loader) {
        List<Class<? extends AbstractModule<?>>> classes = new ArrayList<>();
        for (URL url : loader.getURLs()) {
            try (JarFile file = new JarFile(url.toURI().getPath())) {
                Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    // Only load classes.
                    if (!entry.getName().endsWith(".class"))
                        continue;

                    try {
                        String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                        Class<?> uninitializedClass = Class.forName(className, false, loader);
                        if ((uninitializedClass.getModifiers() & Modifier.ABSTRACT) != 0) // Ignore abstract classes.
                            continue;

                        if (AbstractModule.class.isAssignableFrom(uninitializedClass)) {
                            // Load the class properly.
                            Class<?> initializedClass = loader.loadClass(className);
                            classes.add((Class<? extends AbstractModule<?>>) initializedClass);
                        }
                    } catch (ClassNotFoundException exc) {
                        Platform.get().getLogger().log(Level.WARNING, String.format("Failed to load class %s!", entry.getName()), exc);
                    } catch (Throwable ignored) {
                        // Class file version error ? Either way, wasn't supposed to be loaded so let it pass.
                    }
                }
            } catch (IOException | URISyntaxException exc) {
                Platform.get().getLogger().log(Level.WARNING, String.format("Failed to open jar file %s!", url), exc);
            }
        }
        return classes;
    }

    /**
     * Uses reflection to detect all present module classes and instantiate them.
     *
     * @param classLoader       the plugin classloader to search for
     * @param plugin            the plugin that owns the modules
     * @param platformFilter    skips modules the platform can't run, such as modules made for another server version
     * @param moduleInitializer the initializer for modules
     * @param onDependencyCycle called if the modules depend on each other in a cycle; no module is enabled then
     */
    public static void setupModules(URLClassLoader classLoader, KPluginContext plugin, Predicate<ModuleInfo> platformFilter, Consumer<AbstractModule<?>> moduleInitializer, Runnable onDependencyCycle) {
        //A map storing module classes to their dependencies in graph like formation
        Map<Class<?>, List<Class<?>>> classToClassDependencyGraph = new HashMap<>();
        Map<Class<?>, AbstractModule<?>> instances = new HashMap<>();
        BiMap<String, Class<?>> nameModuleMap = HashBiMap.create();

        discoverModules(classLoader).forEach(mClass -> {
            //We only want to work on our own modules, not other plugins
            if (!mClass.getPackage().getName().startsWith(plugin.getClass().getPackage().getName()))
                return;

            if (!mClass.isAnnotationPresent(ModuleInfo.class))
                return;

            //Instantiate the module with the given information
            ModuleInfo info = mClass.getAnnotation(ModuleInfo.class);

            if (!platformFilter.test(info))
                return;

            AbstractModule<?> instance;
            // Is the module already registered?
            // If so, don't re-register it!
            // This can happen with Kotlin 'object' declarations.
            KNamespacedKey namespace = new KNamespacedKey(plugin, info.name());
            if ((instance = ModuleRegistry.getModule(namespace)) == null) {
                try {
                    Constructor<?> constructor = mClass.getConstructor();
                    instance = (AbstractModule<?>) constructor.newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    // Check for the 'INSTANCE' field which Kotlin objects use.
                    try {
                        Field field = mClass.getDeclaredField("INSTANCE");
                        instance = (AbstractModule<?>) field.get(null);
                    } catch (NoSuchFieldException ignored) {
                        throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), e);
                    } catch (Throwable ex) {
                        throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), ex);
                    }
                } catch (Throwable thr) {
                    // If the module fails to initialize, just skip it.
                    plugin.getPluginLogger().log(Level.SEVERE, "Failed to initialize module " + info.name() + "! It will be skipped!", thr);
                    return;
                }
            }

            instances.put(mClass, instance);
            nameModuleMap.put(info.name(), mClass);
        });

        // Build the dependency graph and filter classes that are missing module dependencies
        instances.entrySet().removeIf(entry -> {
            List<Class<?>> classDepends = new ArrayList<>();
            for (String mdepend : entry.getValue().getModuleDependencies()) {
                if (!nameModuleMap.containsKey(mdepend) && !ModuleRegistry.moduleExists(mdepend)) {
                    nameModuleMap.remove(entry.getValue().getModuleName());
                    return true;
                }

                AbstractModule<?> module = ModuleRegistry.getModule(mdepend);
                if (module == null) {
                    throw new NullPointerException("Registered module under name " + mdepend + " is null!");
                }

                classDepends.add(nameModuleMap.containsKey(mdepend) ? nameModuleMap.get(mdepend) : module.getClass());
            }
            classToClassDependencyGraph.put(entry.getKey(), classDepends);
            return false;
        });

        //Loop through to detect cycles.
        Set<Class<?>> traversed = new HashSet<>();
        Queue<Class<?>> nextUp = new ArrayDeque<>();
        for (Map.Entry<Class<?>, List<Class<?>>> entry : classToClassDependencyGraph.entrySet()) {
            traversed.clear();
            nextUp.add(entry.getKey());
            traversed.add(entry.getKey());

            //Do a breadth first walk to properly detect cycles.
            while (!nextUp.isEmpty()) {
                Class<?> type = nextUp.poll();

                // Skip over classes that aren't in our graph, we don't need to worry about them.
                if (!classToClassDependencyGraph.containsKey(type))
                    continue;

                for (Class<?> value : classToClassDependencyGraph.get(type)) {
                    //If this is the case, we've detected a cycle.
                    if (!traversed.add(value)) {
                        plugin.getPluginLogger().severe(String.format("Detected cycle in dependencies for module '%s'!", entry.getKey().getSimpleName()));
                        onDependencyCycle.run();
                        return;
                    }

                    nextUp.add(value);
                }
            }
        }

        //Do a depth first walk to enable all modules, at this point we know there's no cycles.
        Set<Class<?>> enabled = new HashSet<>();
        Stack<Class<?>> dependencies = new Stack<>();
        for (Map.Entry<Class<?>, List<Class<?>>> entry : classToClassDependencyGraph.entrySet()) {
            if (enabled.contains(entry.getKey()))
                continue;

            dependencies.addAll(entry.getValue());

            //Enable all dependencies.
            while (!dependencies.isEmpty()) {
                //Get the dependency.
                Class<?> currDepend = dependencies.peek();

                // If the class isn't in our graph, it's not our job to enable it.
                if (!classToClassDependencyGraph.containsKey(currDepend)) {
                    dependencies.pop();
                    continue;
                }

                List<Class<?>> depends = classToClassDependencyGraph.get(currDepend);

                //Does it have any dependencies? If so, enable them!
                if (!dependencies.isEmpty() && !enabled.containsAll(depends)) {
                    dependencies.addAll(depends);
                    continue;
                }

                //No depends! Just enable it now.
                dependencies.pop();

                //If it's already been enabled, simply skip it.
                if (!enabled.add(currDepend))
                    continue;

                instances.get(currDepend).initialize();
            }

            enabled.add(entry.getKey());
            AbstractModule<?> mInstance = instances.get(entry.getKey());
            mInstance.initialize();

            moduleInitializer.accept(mInstance);
        }
    }
}
