package com.golfing8.kcommon.struct.time;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class that contains timestamps for things to run at during the day.
 */
public class Schedule {
    /**
     * Time stamps for actions to take place at.
     */
    @Getter
    private final List<ScheduleEntry> scheduleEntries = new ArrayList<>();
    /**
     * A list of durations that should handle an event when it is the duration's length from the next entry.
     * Note that these lengths aren't used in this class. They're primarily used in the {@link ScheduleTask} class,
     * but it's easy to store them in here when creating one.
     */
    @Getter
    private final List<TimeLength> anticipationTimes = new ArrayList<>();

    public Schedule(Collection<Timestamp> entries, Collection<TimeLength> timeLengths) {
        this.scheduleEntries.addAll(entries.stream().map(ScheduleEntry::new).collect(Collectors.toList()));
        this.anticipationTimes.addAll(timeLengths);
    }

    public Schedule(Collection<Timestamp> entries) {
        this.scheduleEntries.addAll(entries.stream().map(ScheduleEntry::new).collect(Collectors.toList()));
    }

    /**
     * Gets all timestamps in this schedule.
     *
     * @return the timestamps.
     */
    public List<Timestamp> getAllTimestamps() {
        return scheduleEntries.stream().map(entry -> entry.timestamp).collect(Collectors.toList());
    }

    /**
     * Adds a time to this schedule.
     * <p>
     * If the timestamp happens to be in the past, it will NOT be run until the NEXT interval.
     * </p>
     *
     * @param timestamp the timestamp.
     */
    public void addTime(Timestamp timestamp) {
        this.scheduleEntries.add(new ScheduleEntry(timestamp));
    }

    /**
     * Gets the next available time stamp from this schedule.
     *
     * @return the timestamp.
     */
    public Timestamp getNextAvailableTimestamp() {
        Timestamp now = Timestamp.now();
        long minimumTime = Long.MAX_VALUE;
        Timestamp found = null;

        for (ScheduleEntry entry : scheduleEntries) {
            long difference = entry.timestamp.getMillisDifference(now);
            if (difference < minimumTime) {
                minimumTime = difference;
                found = entry.timestamp;
            }
        }
        return found;
    }

    /**
     * Polls all available schedule entries and checks if one of them is ready to execute.
     * If one is found to be ready, the timestamp is returned.
     *
     * @return the timestamp if a schedule entry is ready, null if not.
     */
    @Nullable
    public Timestamp checkNextTimestamp() {
        return checkNextTimestamp(Timestamp.now());
    }

    /**
     * Polls all available schedule entries and checks if one of them is ready to execute.
     * If one is found to be ready, the timestamp is returned.
     *
     * @return the timestamp if a schedule entry is ready, null if not.
     */
    @Nullable
    public Timestamp checkNextTimestamp(Timestamp now) {
        Timestamp found = null;
        for (ScheduleEntry entry : scheduleEntries) {
            if (!now.isAfter(entry.timestamp)) {
                entry.yetToHappen = true;
                continue;
            }

            if (!entry.yetToHappen) {
                continue;
            }

            // Don't mark any more off.
            if (found != null) {
                continue;
            }

            entry.yetToHappen = false;
            found = entry.timestamp;
        }
        return found;
    }

    /**
     * Represents an entry in a schedule.
     */
    @RequiredArgsConstructor
    public static final class ScheduleEntry {
        /**
         * The timestamp to schedule on.
         */
        final Timestamp timestamp;
        /**
         * If this action has yet to occur today.
         */
        boolean yetToHappen = false;
    }
}
