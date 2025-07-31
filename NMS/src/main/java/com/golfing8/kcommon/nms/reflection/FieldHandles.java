package com.golfing8.kcommon.nms.reflection;

import com.google.common.collect.Maps;

import java.util.Map;

public class FieldHandles {
    private static final Map<String, FieldHandle<?>> handleCache = Maps.newHashMap();

    private static String getSerializedName(String fieldName, Class<?> clazz){
        return String.format("%s+%s", fieldName, clazz.getName());
    }

    @SuppressWarnings("unchecked")
    public static <T> FieldHandle<T> getHandle(String fieldName, Class<T> clazz){
        String serialized = getSerializedName(fieldName, clazz);

        if(handleCache.containsKey(serialized))
            return (FieldHandle<T>) handleCache.get(serialized);

        FieldHandle<T> newHandle = new FieldHandle<>(fieldName, clazz);
        handleCache.put(serialized, newHandle);
        return newHandle;
    }
}
