package com.golfing8.kcommon.util;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import lombok.SneakyThrows;
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
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Platform independent part of each platform's {@code Reflection}.
 */
public class ReflectionCore {
    protected ReflectionCore() {
    }

    private static final Map<Class<?>, Set<Class<?>>> IMPLEMENTORS_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

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
     * Creates an instance through the class's no-args constructor, even if it is not public.
     *
     * @param clazz the class.
     * @param <T>   the type.
     * @return the instance, or null if the class has no accessible no-args constructor.
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable T newInstance(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);

            return (T) MethodHandles.lookup().unreflectConstructor(constructor).invokeWithArguments();
        } catch (NoSuchMethodException | IllegalAccessException exc) {
            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
