package com.golfing8.kcommon.nms.reflection;

import com.google.common.collect.Maps;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Optional;

/**
 * Contains methods for getting and caching field handles
 */
@UtilityClass
public class FieldHandles {
    private static final Map<String, FieldHandle<?>> handleCache = Maps.newHashMap();

    private static String getSerializedName(String fieldName, Class<?> clazz) {
        return String.format("%s+%s", fieldName, clazz.getName());
    }

    /**
     * Gets the field handle associated with the given field name on the given class
     * <p>
     * Throws {@link RuntimeException} if the field does not exist.
     * </p>
     *
     * @param fieldName the field name
     * @param parent the parent class
     * @return the field handle
     */
    @SuppressWarnings("unchecked")
    public static <T> FieldHandle<T> getHandle(String fieldName, Class<?> parent) throws RuntimeException {
        String serialized = getSerializedName(fieldName, parent);
        if (handleCache.containsKey(serialized))
            return (FieldHandle<T>) handleCache.get(serialized);

        FieldHandle<?> newHandle = new FieldHandle<>(fieldName, parent);
        handleCache.put(serialized, newHandle);
        return (FieldHandle<T>) newHandle;
    }

    /**
     * Gets the field handle associated with the given field name on the given class
     * <p>
     * Returns the empty optional of the field does not exist
     * </p>
     *
     * @param fieldName the field name
     * @param parent the parent class
     * @return the field handle
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<FieldHandle<T>> getHandleOpt(String fieldName, Class<?> parent) {
        String serialized = getSerializedName(fieldName, parent);
        if (handleCache.containsKey(serialized))
            return Optional.of((FieldHandle<T>) handleCache.get(serialized));

        try {
            FieldHandle<T> newHandle = new FieldHandle<>(fieldName, parent);
            handleCache.put(serialized, newHandle);
            return Optional.of(newHandle);
        } catch (RuntimeException exc) {
            return Optional.empty();
        }
    }
}
