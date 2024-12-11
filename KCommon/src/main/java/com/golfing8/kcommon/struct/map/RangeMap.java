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

    /**
     * Gets the minimum key from the ranges stored in this map.
     *
     * @return the minimum key.
     */
    public double getMinimumKey() {
        return this.rangeMap.firstKey();
    }

    /**
     * Gets the maximum key from the ranges stored in this map.
     *
     * @return the maximum key.
     */
    public double getMaximumKey() {
        return this.rangeMap.lastKey();
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
            return this.getEntryPair(((Number) key).doubleValue()) != null;
        }
        return get(key) != null;
    }

    /**
     * An overload to ensure Intellij knows it's fine to use numbers as keys.
     *
     * @param key the key.
     * @return if it contains this key.
     */
    public boolean containsKey(Number key) {
        return this.getEntryPair(key.doubleValue()) != null;
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
    public V get(double dKey) {
        Pair<Range, V> entryPair = getEntryPair(dKey);
        return entryPair == null ? null : entryPair.getB();
    }

    public Pair<Range, V> getEntryPair(double dKey) {
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
            return this.get(((Number) key).doubleValue());
        }
        return this.originalMap.get(key);
    }

    public V put(double key, V value) {
        return put(new Range(key), value);
    }

    @Override
    public V put(Range key, V value) {
        double minimum = key.getMin();
        originalMap.put(key, value);
        Pair<Range, V> valuePair = new Pair<>(key, value);
        Pair<Range, V> old = this.rangeMap.put(minimum, valuePair);
        return old != null ? old.getB() : null;
    }

    /**
     * An overload to ensure intellij knows it's safe to use numbers as keys.
     *
     * @param number the number
     * @return the removed value
     */
    public V remove(Number number) {
        Pair<Range, V> pair = this.getEntryPair(number.doubleValue());
        if(pair == null)
            return null;

        //Remove the values.
        this.originalMap.remove(pair.getA());
        this.rangeMap.remove(pair.getA().getMin());
        return pair.getB();
    }

    @Override
    public V remove(Object key) {
        if(key instanceof Number) {
            return remove((Number) key);
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

    /**
     * Creates a builder for this class.
     *
     * @return the builder.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * A simple builder for range maps.
     */
    public static class Builder<T> {
        private List<Pair<Range, T>> values = new ArrayList<>();
        public Builder<T> put(double key, T object) {
            return put(new Range(key), object);
        }
        public Builder<T> put(Range range, T object) {
            this.values.add(new Pair<>(range, object));
            return this;
        }

        @SuppressWarnings("unchecked")
        public <V> RangeMap<V> build() {
            RangeMap<T> map = new RangeMap<T>();
            for (Pair<Range, T> pair : values) {
                map.put(pair.getA(), pair.getB());
            }
            return (RangeMap<V>) map;
        }
    }
}
