package com.golfing8.kcommon.struct.time;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A task that should be run based upon a given {@link Schedule}
 */
public class ScheduleTask extends BukkitRunnable {
    /** The schedule to run off of */
    private final Schedule schedule;
    /** The action to run when available */
    private final Consumer<Timestamp> action;
    /** A condition in which no new timestamps should be generated */
    private final Supplier<Boolean> pauseCondition;
    /** The rate at which this task ticks */
    @Getter @Setter
    private int tickRate = 20;
    @Getter
    private boolean started = false;

    public ScheduleTask(Schedule schedule, Consumer<Timestamp> action, Supplier<Boolean> pauseCondition) {
        this.schedule = schedule;
        this.action = action;
        this.pauseCondition = pauseCondition;
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
     * Starts running this schedule task.
     */
    public void start() {
        if (started)
            return;

        started = true;
        runTaskTimer(KCommon.getInstance(), 0, this.tickRate);
    }

    @Override
    public void run() {
        if (pauseCondition.get())
            return;

        Timestamp next = this.schedule.checkNextTimestamp();
        if (next != null)
            this.action.accept(next);
    }
}
