package com.golfing8.kcommon.struct;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents an interval between X1 and X2.
 * If X1 is greater than X2, the iterator will iterate backwards.
 */
public class Interval implements Iterable<Double> {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    public static final int STOP = 0;
    public static final int CAP = 1;
    public static final int OVERFLOW = 2;
    public static final int WRAP = 4;

    private static final double TINY_INTERVAL = 1e-6;

    //Package private so communication between iterator and class isn't synthesized.
    @Getter
    double x1, x2;
    @Getter
    double interval;

    //We calculate this number upon construction of this object.
    transient final int intervalSize;

    //Our overflow behavior. Default is STOP
    @Setter
    @Getter
    transient int overflowBehavior = STOP;

    public Interval(double x1, double x2, double interval) {
        this.interval = interval;
        this.x1 = x1;
        this.x2 = x2;

        int size = 0;

        //We loop through our interval
        for (double d : this) {
            size++;
        }

        intervalSize = size;
    }

    /**
     * Gets the effective size of this interval.
     *
     * @return the effective size of this interval.
     */
    public double getIntervalSize() {
        return this.intervalSize;
    }

    public String toSerialString() {
        if (interval != 1.0D) {
            return FORMAT.format(x1) + ";" + FORMAT.format(interval) + ";" + FORMAT.format(x2);
        } else {
            return FORMAT.format(x1) + ";" + FORMAT.format(x2);
        }
    }

    @Override
    public @NonNull Iterator<Double> iterator() {
        return new IntervalIterator();
    }

    public Iterator<Double> tinyIterator() {
        return new IntervalIterator(TINY_INTERVAL);
    }

    private class IntervalIterator implements Iterator<Double> {
        //true if we're iterating in the + direction, false if -.
        private final boolean positive = x1 < x2;
        private final double interval;
        private final int overflowBehavior = Interval.this.overflowBehavior;
        //The current number we're on.
        private double current = x1;

        IntervalIterator() {
            this.interval = Interval.this.interval * (positive ? 1 : -1);
        }

        IntervalIterator(double interval) {
            this.interval = interval * (positive ? 1 : -1);
        }

        @Override
        public boolean hasNext() {
            //If we're on any other behavior than STOP, then there's ALWAYS a next.
            return overflowBehavior != STOP || !Double.isNaN(current);
        }

        @Override
        public Double next() {
            if (!hasNext())
                throw new NoSuchElementException("IntervalIterator already finished!");

            //Save our current state.
            double toReturn = current;

            if (overflowBehavior == OVERFLOW) {
                current += interval;
            }

            //Check if we'll be done next iteration.
            if (positive ? (current == x2) : (current == x1)) {
                //NaN marks our "end".
                //If we're on CAP mode, the function simply "caps" at the end of its interval.
                if (overflowBehavior != CAP)
                    current = Double.NaN;
                //If we're on WRAP mode, set our current back to x1.
                if (overflowBehavior == WRAP)
                    current = x1;
                return toReturn;
            }

            //Check if we'll exceed our bounds.
            if ((positive && current + interval > x2) || (!positive && current + interval < x2)) {
                //Mark our final iteration.
                current = x2;
                return toReturn;
            }

            //Make the modification.
            current += interval;

            return toReturn;
        }
    }

    public static Interval fromString(String string) {
        String[] split = string.split(";");
        if (split.length >= 3) {
            double x1 = Double.parseDouble(split[0]);
            double step = Double.parseDouble(split[1]);
            double x2 = Double.parseDouble(split[2]);
            return new Interval(x1, x2, step);
        } else {
            double x1 = Double.parseDouble(split[0]);
            double x2 = Double.parseDouble(split[1]);
            return new Interval(x1, x2, 1.0D);
        }
    }
}
