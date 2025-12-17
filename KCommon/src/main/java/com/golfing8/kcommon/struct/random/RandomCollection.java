package com.golfing8.kcommon.struct.random;

import com.google.gson.annotations.Expose;

import java.util.*;

/**
 * Represents a random bag that items can be pulled from
 *
 * @param <E> the type stored
 */
public class RandomCollection<E> {

    @Expose
    private final NavigableMap<Double, E> map;
    @Expose
    private double total;

    public RandomCollection() {
        this.map = new TreeMap<>();
        this.total = 0.0D;
    }

    /**
     * Gets the amount of elements in this collection
     *
     * @return the size
     */
    public int size() {
        return this.map.size();
    }

    /**
     * Adds the given item with the given weight to this collection
     *
     * @param weight the weight
     * @param result the result
     */
    public void add(double weight, E result) {
        if (weight > 0.0D) {
            this.total += weight;
            this.map.put(this.total, result);
        }
    }

    /**
     * Gets the next random item in this collection
     *
     * @return the next item
     */
    public E next() {
        if (total > 0) {
            double value = Math.random() * this.total;
            return this.map.higherEntry(value).getValue();
        }
        return null;
    }

    /**
     * Clears this random collection
     */
    public void clear() {
        this.map.clear();
        this.total = 0.0;
    }

    /**
     * Gets an iterator for the given values
     *
     * @return the iterator
     */
    public Iterator<E> getIterator() {
        return map.values().iterator();
    }

    /**
     * Gets a list of all the backing values
     *
     * @return the list
     */
    public List<E> getList() {
        return new ArrayList<>(map.values());
    }

}
