package com.golfing8.kcommon.util;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.nms.reflection.FieldHandle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
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
            try (JarFile file = new JarFile(url.getFile())){
                Enumeration<JarEntry> entries = file.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    // Only load classes.
                    if (!entry.getName().endsWith(".class"))
                        continue;

                    try {
                        Class<?> clazz = loader.loadClass(entry.getName().replace("/", ".").replace(".class", ""));
                        if (Module.class.isAssignableFrom(clazz) && Module.class != clazz)
                            classes.add((Class<? extends Module>) clazz);
                    } catch (ClassNotFoundException exc) {
                        KCommon.getInstance().getLogger().warning(String.format("Failed to load class %s!", entry.getName()));
                        exc.printStackTrace();
                    } catch (Throwable ignored) {
                        // Class file version error ? Either way, wasn't supposed to be loaded so let it pass.
                    }
                }
            } catch (IOException ignored) {}
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
     *     This method would work on a class like this: <br>
     * <code>
     *     public class SomeClass extends ArrayList&lt;String&gt; {}
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
    public static Map<String, FieldHandle<?>> getAllFields(Class<?> clazz) {
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
}
