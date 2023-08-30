package com.golfing8.kcommon.module;

import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * A task that belongs to a module, extending the {@link BukkitRunnable} class to implement itself.
 * All ModuleTask instances are linked to a specific module and cancelled when a module is shutdown.
 */
public class ModuleTask extends BukkitRunnable {
    /**
     * The module this task belongs to.
     */
    @Getter
    private final Module module;
    /**
     * The runnable this task is responsible for.
     */
    @Getter
    private final Runnable task;
    /**
     * If this task has been started/ran.
     */
    private boolean started;

    /**
     * Another constructor to use in the case when the user wants to override the {@link #run()} method.
     *
     * @param module the module this task belongs to.
     */
    public ModuleTask(Module module) {
        this.module = module;
        this.task = () -> {};
    }

    public ModuleTask(Module module, Runnable runnable) {
        this.module = module;
        this.task = runnable;
    }

    @Override
    public void run() {
        this.task.run();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        if (!this.started)
            return;

        super.cancel();
        this.module.removeTask(this);
    }

    /**
     * Schedules this module to run.
     *
     * @return self.
     */
    public ModuleTask start() {
        this.runTask(getModule().getPlugin());
        this.module.addTask(this);
        this.started = true;
        return this;
    }

    /**
     * Schedules this module to run async.
     *
     * @return self.
     */
    public ModuleTask startAsync() {
        this.runTaskAsynchronously(getModule().getPlugin());
        this.module.addTask(this);
        this.started = true;
        return this;
    }

    /**
     * Schedules this module to run after a set time.
     *
     * @param ticks the time, in ticks, for this module to wait.
     * @return self.
     */
    public ModuleTask startLater(long ticks) {
        this.runTaskLater(getModule().getPlugin(), ticks);
        this.module.addTask(this);
        this.started = true;
        return this;
    }

    /**
     * Schedules this module to run after a set time async.
     *
     * @param ticks the time, in ticks, for this module to wait.
     * @return self.
     */
    public ModuleTask startLaterAsync(long ticks) {
        this.runTaskLaterAsynchronously(getModule().getPlugin(), ticks);
        this.module.addTask(this);
        this.started = true;
        return this;
    }

    /**
     * Schedules this module to run on a timer.
     *
     * @param tickDelay the initial delay.
     * @param tickPeriod the period delay.
     * @return self.
     */
    public ModuleTask startTimer(long tickDelay, long tickPeriod) {
        this.runTaskTimer(getModule().getPlugin(), tickDelay, tickPeriod);
        this.module.addTask(this);
        this.started = true;
        return this;
    }

    /**
     * Schedules this module to run on a timer async.
     *
     * @param tickDelay the initial delay.
     * @param tickPeriod the period delay.
     * @return self.
     */
    public ModuleTask startTimerAsync(long tickDelay, long tickPeriod) {
        this.runTaskTimerAsynchronously(getModule().getPlugin(), tickDelay, tickPeriod);
        this.module.addTask(this);
        this.started = true;
        return this;
    }
}
