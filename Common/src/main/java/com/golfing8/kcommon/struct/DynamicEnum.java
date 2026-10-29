package com.golfing8.kcommon.struct;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An enum that can have constants dynamically registered at runtime.
 */
public class DynamicEnum<T extends DynamicEnum<T>> {
    private static final Map<Class<?>, BiMap<String, DynamicEnum<?>>> GLOBAL_REGISTRY = new HashMap<>();

    private final String id;

    /**
     * Constructs an instance of this enum with the given id
     *
     * @param id the id
     */
    protected DynamicEnum(String id) {
        this.id = id;
        GLOBAL_REGISTRY.computeIfAbsent(getClass(), k -> HashBiMap.create()).put(id, this);
    }

    /**
     * Gets the name associated with this enum
     *
     * @return the name
     */
    public String name() {
        return id;
    }

    /**
     * Clears the global registry of all instances of the given dynamic enum
     *
     * @param dynamicEnum the dynamic enum
     */
    public static void clearRegistry(Class<? extends DynamicEnum<?>> dynamicEnum) {
        if (GLOBAL_REGISTRY.containsKey(dynamicEnum)) {
            GLOBAL_REGISTRY.get(dynamicEnum).clear();
        }
    }

    /**
     * Finds the dynamic enum instances on the given universe class with the given ame
     *
     * @param dynamicEnum the dynamic enum
     * @param name the name
     * @return the optional value
     * @param <T> the type of enum
     */
    @SuppressWarnings("unchecked")
    public static <T extends DynamicEnum<T>> Optional<T> valueOf(Class<T> dynamicEnum, String name) {
        return Optional.ofNullable((T) GLOBAL_REGISTRY.computeIfAbsent(dynamicEnum, k -> HashBiMap.create()).get(name));
    }

    /**
     * Gets all values of the given dynamic enum
     *
     * @param dynamicEnum the dynamic enum class
     * @return all values
     * @param <T> the type of enum
     */
    @SuppressWarnings("unchecked")
    public static <T extends DynamicEnum<T>> BiMap<String, T> values(Class<T> dynamicEnum) {
        return (BiMap<String, T>) GLOBAL_REGISTRY.getOrDefault(dynamicEnum, HashBiMap.create());
    }
}
