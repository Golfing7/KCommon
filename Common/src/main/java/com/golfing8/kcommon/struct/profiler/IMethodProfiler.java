package com.golfing8.kcommon.struct.profiler;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * An abstract profiler for methods.
 */
public interface IMethodProfiler {
    /**
     * Dumps the info to console
     */
    void dump();

    /**
     * Resets all profiler data
     */
    void resetData();

    /**
     * Resets profiler data
     * @param key the key to reset
     */
    void resetData(String key);

    /**
     * Starts a profiler on a string key
     * @param key The key to start a profile on
     */
    void start(String key);

    /**
     * Stops a profiler on a string key
     * @param key The key to stop a profiler
     */
    void stop(String key);

    /**
     * Gets the statistics on the given key
     *
     * @param key the key
     * @return the statistics
     */
    @Nullable ProfileStatistics getStatistics(String key);

    /**
     * Gets all statistics and the keys for them
     *
     * @return the statistics
     */
    Map<String, ProfileStatistics> getStatistics();

    /**
     * Gets the default server profiler
     *
     * @return the profiler
     */
    static IMethodProfiler getDefaultProfiler() {
        return HighLowAverageProfiler.INSTANCE;
    }
}
