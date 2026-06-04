package com.golfing8.kcommon.struct.profiler;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A default profiler for recording the timing of things.
 */
public class HighLowAverageProfiler implements IMethodProfiler {
    static final HighLowAverageProfiler INSTANCE = new HighLowAverageProfiler("default");
    private static final DecimalFormat FORMATTER = new DecimalFormat("###,###,###,###,###,###.##");
    private static final int MAX_VALUES_TRACKED = 1000000;

    private final Map<String, LongList> data = new HashMap<>();
    private final Map<String, Long> currentlyProfiling = new HashMap<>();
    private final String key;
    public HighLowAverageProfiler(String key) {
        this.key = key;
    }

    @Override
    public void dump() {
        dumpTo(str -> Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', str)));
    }

    @Override
    public void resetData() {
        data.clear();
    }

    private void dumpTo(Consumer<String> consumer) {
        for (String key : data.keySet()) {
            LongList values = data.get(key);

            ProfileStatistics statistics = ProfileStatistics.construct(values);

            consumer.accept("&b&lDATA ON: " + key);
            consumer.accept("&aSamples: &e" + FORMATTER.format(statistics.getSamples()));
            consumer.accept("&aAverage: &e" + FORMATTER.format(statistics.getAverage()));
            consumer.accept("&aMax: &c" + FORMATTER.format(statistics.getMax()));
            consumer.accept("&aMin: &2" + FORMATTER.format(statistics.getMin()));
            consumer.accept("&aSum: &b" + FORMATTER.format(statistics.getSum()));
            consumer.accept("&aAverage 95%: &e" + FORMATTER.format(statistics.getAverage95()));
            consumer.accept("&aMax 95%: &c" + FORMATTER.format(statistics.getMax95()));
            consumer.accept("&aMin 95%: &2" + FORMATTER.format(statistics.getMin95()));
            consumer.accept("&aSum 95%: &b" + FORMATTER.format(statistics.getSum95()));
            consumer.accept("&aStd Dev: &5" + FORMATTER.format(statistics.getStdDev()));
        }
    }

    @Override
    public void start(String key) {
        //Get time IMMEDIATELY. This way the actual profiled time doesn't include the hashing time from the maps.
        long timeNow = System.nanoTime();
        Long value = currentlyProfiling.get(key);
        if (value != null) {
            currentlyProfiling.remove(key);

            putValue(key, value, timeNow);
        }

        //Get time at last possible second so we can make the time as real as possible.
        currentlyProfiling.put(key, System.nanoTime());
    }

    @Override
    public void stop(String key) {
        //Get time IMMEDIATELY. This way the actual profiled time doesn't include the hashing time from the maps.
        long timeStopped = System.nanoTime();

        if (!currentlyProfiling.containsKey(key))
            return;

        long timeStarted = currentlyProfiling.get(key);

        currentlyProfiling.remove(key);

        putValue(key, timeStarted, timeStopped);
    }

    @Override
    public @Nullable ProfileStatistics getStatistics(String key) {
        LongList numbers = data.get(key);
        if (numbers == null)
            return null;

        return ProfileStatistics.construct(numbers);
    }

    @Override
    public Map<String, ProfileStatistics> getStatistics() {
        Map<String, ProfileStatistics> map = new HashMap<>();
        for (var entry : data.entrySet()) {
            map.put(entry.getKey(), ProfileStatistics.construct(entry.getValue()));
        }
        return map;
    }

    private void putValue(String key, long start, long end) {
        LongList longs = this.data.computeIfAbsent(key, (k) -> new LongArrayList());
        if (longs.size() > MAX_VALUES_TRACKED)
            return;

        longs.add(end - start);
    }
}
