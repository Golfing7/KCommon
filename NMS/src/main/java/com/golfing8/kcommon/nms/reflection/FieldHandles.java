package com.golfing8.kcommon.nms.reflection;

import com.google.common.collect.Maps;

import java.util.Map;

public class FieldHandles {
    private static final Map<String, FieldHandle<?>> handleCache = Maps.newHashMap();

    private static String getSerializedName(String fieldName, Class<?> clazz) {
        return String.format("%s+%s", fieldName, clazz.getName());
    }

    public static FieldHandle<?> getHandle(String fieldName, Class<?> clazz) {
        String serialized = getSerializedName(fieldName, clazz);

        if (handleCache.containsKey(serialized))
            return handleCache.get(serialized);

        FieldHandle<?> newHandle = new FieldHandle<>(fieldName, clazz);
        handleCache.put(serialized, newHandle);
        return newHandle;
    }
}
