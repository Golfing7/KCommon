package com.golfing8.kcommon.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilities for java maps.
 */
public class MapUtil {
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> of(K k, V v, Object... values) {
        Map<K, V> newMap = new HashMap<>();
        newMap.put(k, v);
        for (int i = 0; i < values.length; i += 2) {
            newMap.put((K) values[i], (V) values[i + 1]);
        }
        return newMap;
    }

    public static <K> Map<K, K> ofStrict(K k, K v, K... values) {
        Map<K, K> newMap = new HashMap<>();
        newMap.put(k, v);
        for (int i = 0; i < values.length; i += 2) {
            newMap.put((K) values[i], (K) values[i + 1]);
        }
        return newMap;
    }
}
