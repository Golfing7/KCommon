package com.golfing8.kcommon.struct.profiler;

import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Contains the collected statistics for a profiler
 */
@Data
public class ProfileStatistics {
    private static final DecimalFormat NUM_FORMAT = new DecimalFormat("#.####");
    private final long samples;
    private final double average;
    private final long max;
    private final long min;
    private final long sum;
    private final double average95;
    private final long max95;
    private final long min95;
    private final long sum95;
    private final double stdDev;
    private final long[] data;

    /**
     * Gets a summary json of this object
     *
     * @return the summary
     */
    public JsonObject getSummaryJson() {
        JsonObject object = new JsonObject();
        object.addProperty("samples", samples);
        object.addProperty("average", average);
        object.addProperty("max", max);
        object.addProperty("min", min);
        object.addProperty("sum", sum);
        object.addProperty("average95", average95);
        object.addProperty("max95", max95);
        object.addProperty("min95", min95);
        object.addProperty("sum95", sum95);
        object.addProperty("stddev", stdDev);
        return object;
    }

    /**
     * Converts the statistics into a placeholder container for use in a message
     *
     * @return the placeholder container
     */
    public PlaceholderContainer toPlaceholderContainer() {
        return PlaceholderContainer.compileTrusted(
                "SAMPLES", samples,
                "AVERAGE", NUM_FORMAT.format(average),
                "MAX", max,
                "MIN", min,
                "SUM", sum,
                "AVERAGE_95", NUM_FORMAT.format(average95),
                "MAX_95", max95,
                "MIN_95", min95,
                "SUM_95", sum95,
                "STD_DEV", NUM_FORMAT.format(stdDev)
        );
    }

    /**
     * Constructs statistics based on the list of samples
     *
     * @param samples the samples
     * @return the statistics
     */
    public static ProfileStatistics construct(LongList samples) {
        long sum = 0L;
        long min = Long.MAX_VALUE, max = Long.MIN_VALUE;

        for (long l : samples) {
            sum += l;

            min = Math.min(min, l);
            max = Math.max(max, l);
        }

        double average = (double) sum / samples.size();
        BigDecimal stdDevSum = BigDecimal.ZERO;

        for (long l : samples) {
            stdDevSum = stdDevSum.add(BigDecimal.valueOf(Math.pow(l - average, 2)));
        }
        stdDevSum = stdDevSum.divide(BigDecimal.valueOf(samples.size()), 2, RoundingMode.HALF_UP);

        double stdDev = Math.sqrt(stdDevSum.doubleValue());
        double stdDev2 = stdDev * 2;

        long sum95 = 0;
        long min95 = Long.MAX_VALUE, max95 = Long.MIN_VALUE;
        for (long l : samples) {
            if (l < average - stdDev2 || l > average + stdDev2) {
                continue;
            }

            sum95 += l;
            min95 = Math.min(min95, l);
            max95 = Math.max(max95, l);
        }

        double average95 = (double) sum95 / samples.size();
        return new ProfileStatistics(samples.size(), average, max, min, sum, average95, max95, min95, sum95, stdDev, samples.toLongArray());
    }
}
