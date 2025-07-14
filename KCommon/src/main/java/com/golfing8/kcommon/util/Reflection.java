package com.golfing8.kcommon.util;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
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
 * Contains useful reflection utilities.
 */
public final class Reflection {

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
                        KCommon.getInstance().getLogger().warning(String.format("Failed to load class %s!", entry.getName()));
                        exc.printStackTrace();
                    } catch (Throwable ignored) {
                        // Class file version error ? Either way, wasn't supposed to be loaded so let it pass.
                    }
                }
            } catch (IOException | URISyntaxException exc) {
                exc.printStackTrace();
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

            return (T) clazz.newInstance();
        } catch (NoSuchMethodException e) {
            return supplier.get();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to instantiate " + clazz, e);
        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public static <T> T invokeQuietly(MethodHandle methodHandle, Object... arguments) {
        return (T) methodHandle.invokeWithArguments(arguments);
    }
}
