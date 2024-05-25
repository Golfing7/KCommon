package com.golfing8.kcommon.module;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * A task that belongs to a module, extending the {@link BukkitRunnable} class to implement itself.
 * All ModuleTask instances are linked to a specific module and cancelled when a module is shutdown.
 */
public class ModuleTask<T extends Module> extends BukkitRunnable {
    /**
     * The module this task belongs to.
     */
    @Getter
    private final T module;
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
    public ModuleTask(T module) {
        this.module = module;
        this.task = () -> {};
    }

    public ModuleTask(T module, Runnable runnable) {
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
    public ModuleTask<T> start() {
        this.runTask(getModule().getPlugin());
        return this;
    }

    /**
     * Schedules this module to run async.
     *
     * @return self.
     */
    public ModuleTask<T> startAsync() {
        this.runTaskAsynchronously(getModule().getPlugin());
        return this;
    }

    /**
     * Schedules this module to run after a set time.
     *
     * @param ticks the time, in ticks, for this module to wait.
     * @return self.
     */
    public ModuleTask<T> startLater(long ticks) {
        this.runTaskLater(getModule().getPlugin(), ticks);
        return this;
    }

    /**
     * Schedules this module to run after a set time async.
     *
     * @param ticks the time, in ticks, for this module to wait.
     * @return self.
     */
    public ModuleTask<T> startLaterAsync(long ticks) {
        this.runTaskLaterAsynchronously(getModule().getPlugin(), ticks);
        return this;
    }

    /**
     * Schedules this module to run on a timer.
     *
     * @param tickDelay the initial delay.
     * @param tickPeriod the period delay.
     * @return self.
     */
    public ModuleTask<T> startTimer(long tickDelay, long tickPeriod) {
        this.runTaskTimer(getModule().getPlugin(), tickDelay, tickPeriod);
        return this;
    }

    /**
     * Schedules this module to run on a timer async.
     *
     * @param tickDelay the initial delay.
     * @param tickPeriod the period delay.
     * @return self.
     */
    public ModuleTask<T> startTimerAsync(long tickDelay, long tickPeriod) {
        this.runTaskTimerAsynchronously(getModule().getPlugin(), tickDelay, tickPeriod);
        return this;
    }

    @Override
    public synchronized BukkitTask runTask(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTask(plugin);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    @Override
    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTaskAsynchronously(plugin);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    @Override
    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTaskLater(plugin, delay);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    @Override
    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTaskLaterAsynchronously(plugin, delay);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    @Override
    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTaskTimer(plugin, delay, period);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    @Override
    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
        BukkitTask bukkitTask = super.runTaskTimerAsynchronously(plugin, delay, period);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }
}
