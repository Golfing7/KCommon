package com.golfing8.kcommon.struct.blocks;

import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.map.RangeMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
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
