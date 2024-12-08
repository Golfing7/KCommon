package com.golfing8.kcommon.struct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An enum that can have constants dynamically registered at runtime.
 */
public class DynamicEnum<T extends DynamicEnum<T>> {
    private static final Map<Class<?>, BiMap<String, DynamicEnum<?>>> GLOBAL_REGISTRY = new HashMap<>();

    private final String id;

    protected DynamicEnum(String id) {
        this.id = id;
        GLOBAL_REGISTRY.computeIfAbsent(getClass(), (k) -> HashBiMap.create()).put(id, this);
    }

    public String name() {
        return id;
    }

    public static void clearRegistry(Class<? extends DynamicEnum<?>> dynamicEnum) {
        if (GLOBAL_REGISTRY.containsKey(dynamicEnum)) {
            GLOBAL_REGISTRY.get(dynamicEnum).clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends DynamicEnum<T>> T valueOf(Class<T> dynamicEnum, String name) {
        return (T) GLOBAL_REGISTRY.computeIfAbsent(dynamicEnum, (k) -> HashBiMap.create()).get(name);
    }

    @SuppressWarnings("unchecked")
    public static <T extends DynamicEnum<T>> BiMap<String, T> values(Class<T> dynamicEnum) {
        return (BiMap<String, T>) GLOBAL_REGISTRY.getOrDefault(dynamicEnum, HashBiMap.create());
    }
}
