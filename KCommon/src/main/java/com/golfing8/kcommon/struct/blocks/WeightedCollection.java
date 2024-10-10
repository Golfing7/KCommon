package com.golfing8.kcommon.struct.blocks;

import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.map.RangeMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents a collection of elements that can be randomly chosen, each with their own weights.
 */

public class WeightedCollection<T> {
    /**
     * Maps a given object to its chance to appear.
     */
    private final Map<T, Double> chanceMap;
    /**
     * A map that contains the 'baked' odds of every object.
     */
    private final RangeMap<T> bakedOdds;
    public WeightedCollection() {
        this.chanceMap = new HashMap<>();
        this.bakedOdds = new RangeMap<>();
    }

    @SuppressWarnings("unchecked")
    public WeightedCollection(T value, Double chance, Object... values) {
        this.chanceMap = new HashMap<>();
        this.bakedOdds = new RangeMap<>();

        addWeightedObject(value, chance);
        for (int i = 0; i < values.length; i += 2) {
            addWeightedObject((T) values[i], (Double) values[i + 1]);
        }
    }

    /**
     * Clears this collection.
     */
    public void clear() {
        chanceMap.clear();
        bakedOdds.clear();
    }

    /**
     * Removes the given item from this collection.
     *
     * @param t the item.
     */
    public void remove(T t) {
        this.chanceMap.remove(t);
        bakeOdds();
    }

    /**
     * Removes the given items from this collection.
     *
     * @param items the items to remove.
     */
    public void removeAll(Collection<T> items) {
        for (T item : items) {
            this.chanceMap.remove(item);
        }
        bakeOdds();
    }

    /**
     * Gets an unmodifiable view of the chance map.
     *
     * @return the chance map.
     */
    public Map<T, Double> getChanceMap() {
        return Collections.unmodifiableMap(chanceMap);
    }

    /**
     * Adds a weighted object with chance to the block palette.
     *
     * @param obj the obj to add.
     * @param chance the chance for it to appear. (Should be 0-100)
     */
    public void addWeightedObject(T obj, double chance) {
        this.chanceMap.put(obj, chance);
        this.bakeOdds();
    }

    /**
     * Gets a random object from this collection.
     *
     * @return the object.
     */
    public T get() {
        //We can't give an object if we're empty.
        if(this.bakedOdds.isEmpty())
            return null;

        //Get a random number between 0 and 1, then get the value in the range.
        double inRange = ThreadLocalRandom.current().nextDouble();
        return this.bakedOdds.get(inRange).getB();
    }

    /**
     * Gets all the items in this collection
     *
     * @return all items
     */
    public Set<T> getAll() {
        return Collections.unmodifiableSet(this.chanceMap.keySet());
    }

    /**
     * 'Bakes' the odds in the objectChanceMap into the {@link #bakedOdds} map for easy lookups.
     */
    private void bakeOdds() {
        this.bakedOdds.clear();
        double totalChanceSum = 0.0D;
        for(Double value : this.chanceMap.values()) {
            totalChanceSum += value;
        }

        //Loop over all entries, adding the object and its normalized entry.
        double lastEntryStart = 0.0D;
        for(Map.Entry<T, Double> entry : this.chanceMap.entrySet()) {
            double normalChance = entry.getValue() / totalChanceSum;
            this.bakedOdds.put(new Range(lastEntryStart, lastEntryStart + normalChance), entry.getKey());
            lastEntryStart += normalChance;
        }
    }
}
