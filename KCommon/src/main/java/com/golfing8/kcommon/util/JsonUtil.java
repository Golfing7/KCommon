package com.golfing8.kcommon.util;

import com.google.gson.JsonElement;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Array;

/**
 * A utility class for GSON types.
 */
@UtilityClass
public class JsonUtil {

    /**
     * Tries to read the json element as the given class type.
     *
     * @param element the json element.
     * @param clazz   the class type.
     * @param <T>     the type
     * @return the found object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T readByType(JsonElement element, Class<T> clazz) {
        // Check for array types first.
        if (clazz.isArray()) {
            if (!element.isJsonArray()) {
                throw new IllegalArgumentException("Class was an array but json element was not!");
            }

            Class<?> componentType = clazz.getComponentType();
            Object[] arr = (Object[]) Array.newInstance(componentType, element.getAsJsonArray().size());
            int ind = 0;
            for (JsonElement jElement : element.getAsJsonArray()) {
                arr[ind] = readByType(jElement, componentType);
            }
            return (T) arr;
        }

        if (clazz == Double.class) {
            return (T) Double.valueOf(element.getAsDouble());
        } else if (clazz == Float.class) {
            return (T) Float.valueOf(element.getAsFloat());
        } else if (clazz == Long.class) {
            return (T) Long.valueOf(element.getAsLong());
        } else if (clazz == Integer.class) {
            return (T) Integer.valueOf(element.getAsInt());
        } else if (clazz == Short.class) {
            return (T) Short.valueOf(element.getAsShort());
        } else if (clazz == Byte.class) {
            return (T) Byte.valueOf(element.getAsByte());
        } else if (clazz == String.class) {
            return (T) element.getAsString();
        }
        throw new IllegalArgumentException(String.format("No json type %s", clazz.getName()));
    }
}
