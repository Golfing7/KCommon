package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for java maps.
 */
@UtilityClass
public class MapUtil {
    /**
     * Constructs a map of the given data
     *
     * @param k the first key value
     * @param v the second key value
     * @param values the remaining values
     * @return the map
     * @param <K> the key type
     * @param <V> the value type
     */
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> of(K k, V v, Object... values) {
        Map<K, V> newMap = new HashMap<>();
        newMap.put(k, v);
        for (int i = 0; i < values.length; i += 2) {
            newMap.put((K) values[i], (V) values[i + 1]);
        }
        return newMap;
    }

    /**
     * Constructs a map with the given key value pairs
     *
     * @param k the first key
     * @param v the first value
     * @param values the values
     * @return the map
     * @param <K> the key/value types
     */
    @SafeVarargs
    public static <K> Map<K, K> ofStrict(K k, K v, K... values) {
        Map<K, K> newMap = new HashMap<>();
        newMap.put(k, v);
        for (int i = 0; i < values.length; i += 2) {
            newMap.put(values[i], values[i + 1]);
        }
        return newMap;
    }

    /**
     * Fills a given map with the given key value pairs
     *
     * @param map the map
     * @param values the values
     * @return the map
     * @param <K> the key type
     * @param <V> the value type
     * @param <T> the map type
     */
    @SuppressWarnings("unchecked")
    public static <K, V, T extends Map<K, V>> T fill(T map, Object... values) {
        for (int i = 0; i < values.length; i += 2) {
            map.put((K) values[i], (V) values[i + 1]);
        }
        return map;
    }
}
