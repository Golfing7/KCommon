package com.golfing8.kcommon.util;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.PluginClassLoader;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Contains useful reflection utilities.
 */
@UtilityClass
public final class Reflection {

    private static final Map<Class<?>, Set<Class<?>>> IMPLEMENTORS_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Discovers all classes that extend module except for {@link Module}.
     *
     * @param loader the class loader to check under.
     * @return the modules.
     */
    @SuppressWarnings("unchecked")
    public static Collection<Class<? extends Module>> discoverModules(URLClassLoader loader) {
        List<Class<? extends Module>> classes = new ArrayList<>();
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

                        if (Module.class.isAssignableFrom(uninitializedClass) && Module.class != uninitializedClass) {
                            // Load the class properly.
                            Class<?> initializedClass = loader.loadClass(className);
                            classes.add((Class<? extends Module>) initializedClass);
                        }
                    } catch (ClassNotFoundException exc) {
                        KCommon.getInstance().getLogger().log(Level.WARNING, String.format("Failed to load class %s!", entry.getName()), exc);
                    } catch (Throwable ignored) {
                        // Class file version error ? Either way, wasn't supposed to be loaded so let it pass.
                    }
                }
            } catch (IOException | URISyntaxException exc) {
                KCommon.getInstance().getLogger().log(Level.WARNING, String.format("Failed to open jar file %s!", url), exc);
            }
        }
        return classes;
    }

    /**
     * Gets the parameterized types of the given field.
     *
     * @param field the field.
     * @return the parameterized types.
     */
    public static List<Type> getParameterizedTypes(Field field) {
        List<Type> classes = new ArrayList<>();
        Type fieldType = field.getGenericType();
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            classes.addAll(Arrays.asList(parameterizedType.getActualTypeArguments()));
        }
        return classes;
    }

    /**
     * Gets the parameterized types of the given class' super.
     * <p>
     * This method would work on a class like this: <br>
     * <code>
     * public class SomeClass extends ArrayList&lt;String&gt; {}
     * </code>
     * </p>
     *
     * @param clazz the class.
     * @return the parameterized types.
     */
    public static List<Class<?>> getSuperParameterizedTypes(Class<?> clazz) {
        List<Class<?>> classes = new ArrayList<>();
        Type fieldType = clazz.getGenericSuperclass();
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) fieldType;
            for (Type t : parameterizedType.getActualTypeArguments()) {
                if (!(t instanceof Class))
                    continue;

                classes.add((Class<?>) t);
            }
        }
        return classes;
    }

    /**
     * Gets all declared or accessible fields for the given class.
     *
     * @param clazz the clazz.
     * @return all fields, mapped from their name.
     */
    public static Set<Field> getAllFields(Class<?> clazz) {
        Set<Field> fields = new HashSet<>();
        Collections.addAll(fields, clazz.getDeclaredFields());
        Collections.addAll(fields, clazz.getFields());
        return fields;
    }

    /**
     * Gets all declared or accessible field handles for the given class.
     *
     * @param clazz the clazz.
     * @return all fields, mapped from their name.
     */
    public static Map<String, FieldHandle<?>> getAllFieldHandles(Class<?> clazz) {
        Map<String, FieldHandle<?>> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            FieldHandle<?> handle = new FieldHandle<>(field);
            map.put(field.getName(), handle);
        }

        for (Field field : clazz.getFields()) {
            if (map.containsKey(field.getName()))
                continue;

            FieldHandle<?> handle = new FieldHandle<>(field);
            map.put(field.getName(), handle);
        }
        return map;
    }

    /**
     * Gets ALL fields regardless of openness of the given class up to the parent class.
     * <p>
     * Particularly, we keep gathering fields of classes, until {@link Class#isAssignableFrom(Class)} returns false with the parent class.
     * </p>
     *
     * @param clazz  the class.
     * @param parent the parent class to reach to.
     * @return all fields up to and including the parent class.
     */
    public static Map<String, FieldHandle<?>> getAllFieldHandlesUpToIncluding(Class<?> clazz, Class<?> parent) {
        Map<String, FieldHandle<?>> fields = new HashMap<>();
        Class<?> current = clazz;
        while (current != Object.class && parent.isAssignableFrom(current)) {
            for (Field field : current.getDeclaredFields()) {
                if (fields.containsKey(field.getName()))
                    continue;

                FieldHandle<?> handle = new FieldHandle<>(field);
                fields.put(field.getName(), handle);
            }
            current = current.getSuperclass();
        }

        for (Field field : clazz.getFields()) {
            if (fields.containsKey(field.getName()))
                continue;

            FieldHandle<?> handle = new FieldHandle<>(field);
            fields.put(field.getName(), handle);
        }
        return fields;
    }

    /**
     * Gets ALL fields regardless of openness of the given class up to the parent class.
     * <p>
     * Particularly, we keep gathering fields of classes, until {@link Class#isAssignableFrom(Class)} returns false with the parent class.
     * </p>
     *
     * @param clazz  the class.
     * @param parent the parent class to reach to.
     * @return all fields up to and including the parent class.
     */
    public static Set<Field> getAllFieldsUpToIncluding(Class<?> clazz, Class<?> parent) {
        Set<Field> fields = new HashSet<>();
        Class<?> current = clazz;
        while (current != Object.class && parent.isAssignableFrom(current)) {
            Collections.addAll(fields, current.getDeclaredFields());
            current = current.getSuperclass();
        }
        Collections.addAll(fields, clazz.getFields()); // Finally take care of public fields and such...
        return fields;
    }

    /**
     * Gets all fields on a class with a given annotation.
     *
     * @param clazz     the class.
     * @param annoClass the annotation's class.
     * @return the fields with that annotation.
     */
    public static Set<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annoClass) {
        Set<Field> fields = new HashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(annoClass) == null)
                continue;

            fields.add(field);
        }

        for (Field field : clazz.getFields()) {
            if (field.getAnnotation(annoClass) == null)
                continue;

            fields.add(field);
        }
        return fields;
    }

    /**
     * Gets all static nested classes inside the given class.
     *
     * @param main the main class.
     * @return the nested classes
     */
    public static Set<Class<?>> getAllNestedClasses(Class<?> main) {
        Set<Class<?>> classes = new HashSet<>();
        for (Class<?> clazz : main.getDeclaredClasses()) {
            classes.add(clazz);
            classes.addAll(getAllNestedClasses(clazz));
        }
        return classes;
    }

    /**
     * Gets all direct or indirect implementors of the given class
     *
     * @param clazz the class
     * @return the set of implementors, NOT including the base class.
     * @param <T> the type T
     */
    public static <T> Set<Class<? extends T>> getAllImplementors(Class<T> clazz) {
        @SuppressWarnings({"rawtypes", "unchecked"})
        Set<Class<? extends T>> cached = (Set) IMPLEMENTORS_CACHE.get(clazz);
        if (cached != null)
            return new HashSet<>(cached);

        Set<Class<? extends T>> classes = new HashSet<>();
        ClassLoader loader = clazz.getClassLoader();
        if (!(loader instanceof URLClassLoader)) {
            IMPLEMENTORS_CACHE.put(clazz, new HashSet<>(classes));
            return classes;
        }

        for (URL url : ((URLClassLoader) loader).getURLs()) {
            try {
                File file = new File(url.toURI());
                if (file.isDirectory()) {
                    Deque<File> stack = new ArrayDeque<>();
                    stack.push(file);
                    while (!stack.isEmpty()) {
                        File current = stack.pop();
                        File[] children = current.listFiles();
                        if (children == null)
                            continue;

                        for (File child : children) {
                            if (child.isDirectory()) {
                                stack.push(child);
                                continue;
                            }

                            if (!child.getName().endsWith(".class"))
                                continue;

                            String relativePath = file.toPath().relativize(child.toPath()).toString();
                            String className = relativePath.replace(File.separatorChar, '.').replaceAll("\\.class$", "");
                            try {
                                getAllImplementorsNested(clazz, classes, loader, className);
                            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                                // Skip classes that cannot be loaded in this context.
                            }
                        }
                    }
                    continue;
                }

                try (JarFile fileJar = new JarFile(file)) {
                    Enumeration<JarEntry> entries = fileJar.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (!entry.getName().endsWith(".class"))
                            continue;

                        try {
                            String className = entry.getName().replace("/", ".").replaceAll("\\.class$", "");
                            getAllImplementorsNested(clazz, classes, loader, className);
                        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                            // Skip classes that cannot be loaded in this context.
                        }
                    }
                }
            } catch (IOException | URISyntaxException ignored) {
                // Skip malformed or unreadable classpath entries.
            }
        }

        IMPLEMENTORS_CACHE.put(clazz, new HashSet<>(classes));
        return classes;
    }

    @SuppressWarnings("unchecked")
    private static <T> void getAllImplementorsNested(Class<T> clazz, Set<Class<? extends T>> classes, ClassLoader loader, String className) throws ClassNotFoundException {
        Class<?> candidate = Class.forName(className, false, loader);
        if (candidate == clazz || candidate.isInterface() || Modifier.isAbstract(candidate.getModifiers()))
            return;

        if (clazz.isAssignableFrom(candidate)) {
            classes.add((Class<? extends T>) candidate);
        }
    }

    /**
     * Finds a getter for the given field on the given class
     *
     * @param clazz the class
     * @param name the name of the field
     * @return the getter method handle
     */
    public static @Nullable MethodHandle findGetter(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectGetter(field);
        } catch (NoSuchFieldException | IllegalAccessException exc) {
            return null;
        }
    }

    /**
     * Finds a setter for the given field on the given class
     *
     * @param clazz the class
     * @param name the name of the field
     * @return the setter method handle
     */
    public static @Nullable MethodHandle findSetter(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return MethodHandles.lookup().unreflectSetter(field);
        } catch (NoSuchFieldException | IllegalAccessException exc) {
            return null;
        }
    }

    /**
     * Gets a method handle for the given information.
     *
     * @param clazz          the class
     * @param name           the name
     * @param parameterTypes the types
     * @return the method handle, or null if not found.
     */
    public static @Nullable MethodHandle findMethodHandle(Class<?> clazz, String name, Class<?>... parameterTypes) {
        try {
            Method method = clazz.getDeclaredMethod(name, parameterTypes);
            method.setAccessible(true);

            return MethodHandles.lookup().unreflect(method);
        } catch (NoSuchMethodException | IllegalAccessException exc) {
            return null;
        }
    }

    /**
     * Gets a constructor handle for the given information.
     *
     * @param clazz          the class
     * @param parameterTypes the types
     * @return the constructor handle, or null if not found.
     */
    public static @Nullable MethodHandle findConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);

            return MethodHandles.lookup().unreflectConstructor(constructor);
        } catch (NoSuchMethodException | IllegalAccessException exc) {
            return null;
        }
    }

    /**
     * Gets the class for the given name, or an empty optional
     *
     * @param name the name
     * @return the optional
     */
    public static Optional<Class<?>> forNameOptional(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException exc) {
            return Optional.empty();
        }
    }

    /**
     * Instantiates an instance of the given class or returns the value by the supplier.
     *
     * @param clazz    the class type
     * @param supplier the supplier
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiateOrGet(Class<?> clazz, Supplier<T> supplier) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            return (T) constructor.newInstance();
        } catch (NoSuchMethodException e) {
            return supplier.get();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Failed to instantiate " + clazz, e);
        }
    }

    /**
     * Invokes the given method handle quietly
     *
     * @param methodHandle the method handle to invoke
     * @param arguments the arguments for the method handle
     * @return the value returned
     * @param <T> the return type
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T invokeQuietly(MethodHandle methodHandle, Object... arguments) {
        return (T) methodHandle.invokeWithArguments(arguments);
    }

    /**
     * Uses reflection to detect all present module classes and instantiate them.
     *
     * @param classLoader the plugin classloader to search for
     * @param moduleInitializer the initializer for modules
     */
    public static void reflectivelySetupModules(PluginClassLoader classLoader, Consumer<Module> moduleInitializer) {
        //A map storing module classes to their dependencies in graph like formation
        Map<Class<?>, List<Class<?>>> classToClassDependencyGraph = new HashMap<>();
        Map<Class<?>, Module> instances = new HashMap<>();
        BiMap<String, Class<?>> nameModuleMap = HashBiMap.create();

        NMSVersion serverVersion = KCommon.getInstance().getServerVersion();
        Reflection.discoverModules(classLoader).forEach(mClass -> {
            //We only want to work on our own modules, not other plugins
            if (!mClass.getPackage().getName().startsWith(classLoader.getPlugin().getClass().getPackage().getName()))
                return;

            if (!mClass.isAnnotationPresent(ModuleInfo.class))
                return;

            //Instantiate the module with the given information
            ModuleInfo info = mClass.getAnnotation(ModuleInfo.class);

            // We can filter modules that are missing plugin dependencies at this point.
            for (String depend : info.pluginDependencies()) {
                if (!Bukkit.getPluginManager().isPluginEnabled(depend)) {
                    return;
                }
            }

            // Check if the module is running on the proper server version.
            if (info.minimumMajorVersion() > 0 && serverVersion.getMajor() < info.minimumMajorVersion()) {
                return;
            }
            if (info.maximumMajorVersion() > 0 && serverVersion.getMajor() > info.maximumMajorVersion()) {
                return;
            }
            if (info.minimumMinorVersion() > 0 && serverVersion.getMinor() < info.minimumMajorVersion()) {
                return;
            }
            if (info.maximumMinorVersion() > 0 && serverVersion.getMinor() > info.maximumMinorVersion()) {
                return;
            }

            Module instance;
            // Is the module already registered?
            // If so, don't re-register it!
            // This can happen with Kotlin 'object' declarations.
            KNamespacedKey namespace = new KNamespacedKey(classLoader.getPlugin(), info.name());
            if ((instance = Modules.getModule(namespace)) == null) {
                try {
                    Constructor<? extends Module> constructor = mClass.getConstructor();
                    instance = constructor.newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    // Check for the 'INSTANCE' field which Kotlin objects use.
                    try {
                        Field field = mClass.getDeclaredField("INSTANCE");
                        instance = (Module) field.get(null);
                    } catch (NoSuchFieldException ignored) {
                        throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), e);
                    } catch (Throwable ex) {
                        throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), ex);
                    }
                } catch (Throwable thr) {
                    // If the module fails to initialize, just skip it.
                    classLoader.getPlugin().getLogger().log(Level.SEVERE, "Failed to initialize module " + info.name() + "! It will be skipped!", thr);
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
                if (!nameModuleMap.containsKey(mdepend) && !Modules.moduleExists(mdepend)) {
                    nameModuleMap.remove(entry.getValue().getModuleName());
                    return true;
                }

                Module module = Modules.getModule(mdepend);
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
                        classLoader.getPlugin().getLogger().severe(String.format("Detected cycle in dependencies for module '%s'!", entry.getKey().getSimpleName()));
                        classLoader.getPlugin().getServer().getPluginManager().disablePlugin(classLoader.getPlugin());
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
            Module mInstance = instances.get(entry.getKey());
            mInstance.initialize();

            moduleInitializer.accept(mInstance);
        }
    }
}
