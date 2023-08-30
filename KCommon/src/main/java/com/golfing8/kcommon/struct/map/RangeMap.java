package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.struct.Pair;
import com.golfing8.kcommon.struct.Range;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores a range as a key to a given value. Assumes that there are no overlaps.
 */
public class RangeMap<V> implements Map<Range, V> {
    /**
     * The map backing this range map.
     */
    private final TreeMap<Double, Pair<Range, V>> rangeMap;
    /**
     * Stores the originally mapped ranges to their values.
     */
    private final Map<Range, V> originalMap;

    public RangeMap() {
        this.rangeMap = new TreeMap<>();
        this.originalMap = new HashMap<>();
    }

    @Override
    public int size() {
        return this.rangeMap.size();
    }

    @Override
    public boolean isEmpty() {
        return this.rangeMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if(key instanceof Number) {
            return this.get(((Number) key).doubleValue()) != null;
        }
        return get(key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return this.rangeMap.containsValue(value);
    }

    /**
     * Gets the value from the given number.
     *
     * @param dKey the number.
     * @return the value.
     */
    public Pair<Range, V> get(double dKey) {
        Entry<Double, Pair<Range, V>> floor = this.rangeMap.floorEntry(dKey);
        if(floor == null) {
            return null;
        }

        //Then check if the number is within its range.
        if(!floor.getValue().getA().inRange(dKey)) {
            return null;
        }

        //Otherwise, we've found the value.
        return floor.getValue();
    }

    @Override
    public V get(Object key) {
        if(key instanceof Number) {
            return this.get(((Number) key).doubleValue()).getB();
        }
        return this.originalMap.get(key);
    }

    @Override
    public V put(Range key, V value) {
        double minimum = key.getMin();
        Pair<Range, V> valuePair = new Pair<>(key, value);
        Pair<Range, V> old = this.rangeMap.put(minimum, valuePair);
        return old != null ? old.getB() : null;
    }

    @Override
    public V remove(Object key) {
        if(key instanceof Number) {
            Pair<Range, V> pair = this.get(((Number) key).doubleValue());
            if(pair == null)
                return null;

            //Remove the values.
            this.originalMap.remove(pair.getA());
            this.rangeMap.remove(pair.getA().getMin());
            return pair.getB();
        }

        //Non ranges not supported here.
        if(!(key instanceof Range))
            return null;

        //Remove the key and return the value.
        V removed = this.originalMap.remove(key);
        this.rangeMap.remove(((Range) key).getMin());
        return removed;
    }

    @Override
    public void putAll(Map<? extends Range, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.rangeMap.clear();
    }

    @Override
    public Set<Range> keySet() {
        return this.rangeMap.values().stream().map(Pair::getA).collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return this.rangeMap.values().stream().map(Pair::getB).collect(Collectors.toSet());
    }

    @Override
    public Set<Entry<Range, V>> entrySet() {
        return originalMap.entrySet();
    }
}
