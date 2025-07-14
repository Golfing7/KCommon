package com.golfing8.kcommon.struct;

/**
 * A rolling average, keeping track of a number over a set time, the size, in ticks.
 */
public class RollingAverage {
    private final int size;
    private double total;
    private int index = 0;
    private final double[] samples;

    public RollingAverage(int size) {
        this.size = size;
        this.total = 0;
        this.samples = new double[size];
        for (int i = 0; i < size; i++) {
            this.samples[i] = 0;
        }
    }

    public void editHead(double edit) {
        total += edit;

        samples[index] += edit;
    }

    public void add(double amount) {
        total -= samples[index];
        samples[index] = amount;
        total += amount;
        if (++index == size) {
            index = 0;
        }
    }

    public double getTotal() {
        return total / (size / 20D);
    }
}