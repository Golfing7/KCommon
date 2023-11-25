package com.golfing8.kcommon.struct.time;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A task that should be run based upon a given {@link Schedule}
 */
public class ScheduleTask extends BukkitRunnable {
    private static final TimeLength MAX_ANTICIPATE_LENGTH = new TimeLength(100); // 5 seconds

    /** The schedule to run off of */
    private final Schedule schedule;
    /** The action to run when available */
    private final Consumer<Timestamp> action;
    /** A condition in which no new timestamps should be generated */
    private final Supplier<Boolean> pauseCondition;
    /** Keeps track of the timestamps that have been run */
    private final TreeMap<TimeLength, Boolean> ranTimestamps = new TreeMap<>();
    /** An optional consumer for handling events where it's X time UNTIL an action. */
    @Getter @Setter @Nullable
    private Consumer<TimeLength> anticipateTask;
    /** The rate at which this task ticks */
    @Getter @Setter
    private int tickRate = 20;
    @Getter
    private boolean started = false;

    public ScheduleTask(Schedule schedule, Consumer<Timestamp> action, Supplier<Boolean> pauseCondition) {
        this.schedule = schedule;
        this.action = action;
        this.pauseCondition = pauseCondition;
        this.schedule.getAnticipationTimes().forEach(timestamp -> this.ranTimestamps.put(timestamp, true));
    }

    public ScheduleTask(Schedule schedule, Runnable runnable, Supplier<Boolean> pauseCondition) {
        this(schedule, (stamp) -> runnable.run(), pauseCondition);
    }

    public ScheduleTask(Schedule schedule, Consumer<Timestamp> action) {
        this(schedule, action, () -> false);
    }

    public ScheduleTask(Schedule schedule, Runnable runnable) {
        this(schedule, (stamp) -> runnable.run());
    }

    /**
     * Adds a time to the anticipated task.
     *
     * @param length the time length.
     */
    public void addAnticipatedTime(TimeLength length) {
        this.ranTimestamps.put(length, true);
    }

    /**
     * Starts running this schedule task.
     */
    public void start() {
        if (started)
            return;

        started = true;
        runTaskTimer(KCommon.getInstance(), 0, this.tickRate);
    }

    /**
     * Checks the upcoming anticipated times and tries to run the task.
     */
    private void checkAnticipatedTimes() {
        if (this.anticipateTask == null)
            return;

        Timestamp now = Timestamp.now();
        Timestamp nextAvailable = this.schedule.getNextAvailableTimestamp().clone();
        // Support time lengths of seconds.
        if (nextAvailable.getSecond() == Timestamp.UNUSED)
            nextAvailable.setSecond(0);

        long difference = nextAvailable.getMillisDifference(now);
        if (difference < 0)
            return;

        TimeLength timeRemaining = new TimeLength(difference / 50);
        var canAnnounce = this.ranTimestamps.ceilingEntry(timeRemaining);
        if (canAnnounce == null || !canAnnounce.getValue())
            return;

        // Check the actual difference in time. We *really* don't want to print this out if it's not actually close to that time.
        TimeLength actualTime = canAnnounce.getKey();
        long tickDifference = Math.abs(actualTime.getDurationTicks() - timeRemaining.getDurationTicks());
        this.ranTimestamps.put(canAnnounce.getKey(), false); // Entry.setValue isn't supported.
        if (tickDifference > MAX_ANTICIPATE_LENGTH.getDurationTicks())
            return;

        this.anticipateTask.accept(canAnnounce.getKey());
    }

    @Override
    public void run() {
        if (pauseCondition.get())
            return;

        // Check the anticipation task.
        checkAnticipatedTimes();

        Timestamp next = this.schedule.checkNextTimestamp();
        if (next == null)
            return;

        // Reset all anticipated times.
        for (var entry : this.ranTimestamps.entrySet()) {
            entry.setValue(true);
        }
        this.action.accept(next);
    }
}
