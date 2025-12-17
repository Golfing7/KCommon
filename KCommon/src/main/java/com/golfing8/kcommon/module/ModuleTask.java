package com.golfing8.kcommon.module;

import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

/**
 * A task that belongs to a module, extending the {@link BukkitRunnable} class to implement itself.
 * All ModuleTask instances are linked to a specific module and cancelled when a module is shutdown.
 */
public class ModuleTask<T extends Module> implements Terminable {
    /**
     * The module this task belongs to.
     */
    @Getter
    private final T module;
    /**
     * The runnable this task is responsible for.
     */
    private final Runnable registeredTask;
    /**
     * The runnable that has been passed in
     */
    private final Consumer<Terminable> delegateTask;
    /**
     * The running bukkit task associated with this module task
     */
    private BukkitTask bukkitTask;
    /**
     * If this task has been started/ran.
     */
    @Getter
    private boolean started;
    /**
     * If this is a timer type of task
     */
    private boolean timerTask;
    /**
     * If this task has been run at least once
     */
    @Getter
    private boolean ran;
    /**
     * The amount of times this task has been run
     */
    @Getter
    private int runs;

    protected ModuleTask(T module) {
        this.module = module;
        this.delegateTask = t -> this.run();
        this.registeredTask = this::runInternal;
    }

    public ModuleTask(T module, Runnable runnable) {
        this.module = module;
        this.delegateTask = t -> runnable.run();
        this.registeredTask = this::runInternal;
    }

    public ModuleTask(T module, Consumer<Terminable> ticker) {
        this.module = module;
        this.delegateTask = ticker;
        this.registeredTask = this::runInternal;
    }

    private void runInternal() {
        this.ran = true;
        this.runs++;
        this.delegateTask.accept(this);
        if (!this.timerTask) {
            this.module.removeTask(this);
        }
    }

    protected void run() {
    }

    /**
     * Cancel the task
     *
     * @throws IllegalStateException if the task was already canceled
     */
    public synchronized void cancel() throws IllegalStateException {
        if (!this.started)
            return;

        this.module.removeTask(this);
        this.bukkitTask.cancel();
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
     * @param tickDelay  the initial delay.
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
     * @param tickDelay  the initial delay.
     * @param tickPeriod the period delay.
     * @return self.
     */
    public ModuleTask<T> startTimerAsync(long tickDelay, long tickPeriod) {
        this.runTaskTimerAsynchronously(getModule().getPlugin(), tickDelay, tickPeriod);
        return this;
    }

    @Override
    public void close() {
        this.cancel();
    }

    /**
     * Runs the task with the given plugin
     *
     * @param plugin the plugin
     * @return the generated task
     */
    public synchronized BukkitTask runTask(Plugin plugin) {
        this.bukkitTask = Bukkit.getScheduler().runTask(plugin, registeredTask);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    /**
     * Runs the task with the given plugin asynchronously
     *
     * @param plugin the plugin
     * @return the generated task
     */
    public synchronized BukkitTask runTaskAsynchronously(Plugin plugin) {
        this.bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, registeredTask);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    /**
     * Runs the task with the given plugin after a delay
     *
     * @param plugin the plugin
     * @param delay the delay
     * @return the generated task
     */
    public synchronized BukkitTask runTaskLater(Plugin plugin, long delay) {
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, registeredTask, delay);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    /**
     * Runs the task with the given plugin after a delay and asynchronously
     *
     * @param plugin the plugin
     * @param delay the delay
     * @return the generated task
     */
    public synchronized BukkitTask runTaskLaterAsynchronously(Plugin plugin, long delay) {
        this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, registeredTask, delay);
        this.module.addTask(this);
        this.started = true;
        return bukkitTask;
    }

    /**
     * Runs the task with the given plugin after a delay and repeating every period
     *
     * @param plugin the plugin
     * @param delay the delay
     * @param period the period
     * @return the generated task
     */
    public synchronized BukkitTask runTaskTimer(Plugin plugin, long delay, long period) {
        this.bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, registeredTask, delay, period);
        this.module.addTask(this);
        this.started = true;
        this.timerTask = true;
        return bukkitTask;
    }

    /**
     * Runs the task with the given plugin asynchronously, after a delay and repeating every period
     *
     * @param plugin the plugin
     * @param delay the delay
     * @param period the period
     * @return the generated task
     */
    public synchronized BukkitTask runTaskTimerAsynchronously(Plugin plugin, long delay, long period) {
        this.bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, registeredTask, delay, period);
        this.module.addTask(this);
        this.started = true;
        this.timerTask = true;
        return bukkitTask;
    }
}
