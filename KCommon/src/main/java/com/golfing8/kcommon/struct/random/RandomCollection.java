package com.golfing8.kcommon.struct.random;

import com.google.gson.annotations.Expose;

import java.util.*;

public class RandomCollection<E> {

    @Expose private final NavigableMap<Double, E> map;
    @Expose private double total;

    public RandomCollection() {
        this.map = new TreeMap<>();
        this.total = 0.0D;
    }

    public int size() {
        return this.map.size();
    }

    public void add(double weight, E result) {
        if (weight > 0.0D) {
            this.total += weight;
            this.map.put(this.total, result);
        }
    }

    public void remove() {
        map.clear();
    }

    public double randomChance() {
        return Math.random() * 100.0D;
    }

    public E next() {
        if (total > 0) {
            double value = Math.random() * this.total;
            return this.map.higherEntry(value).getValue();
        }
        return null;
    }

    public E get(int i) {
        if (i >= 0 && i < this.map.size()) {
            Object obj = this.map.keySet().toArray()[i];
            return this.map.get(obj);
        }
        return null;
    }

    public void destroy() {
        this.map.clear();
        this.total = 0.0;
    }

    public Iterator<E> getIterator() {
        return map.values().iterator();
    }

    public List<E> getList() {
        return new ArrayList<>(map.values());
    }

}
