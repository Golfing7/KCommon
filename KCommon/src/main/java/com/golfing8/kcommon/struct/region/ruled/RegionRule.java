package com.golfing8.kcommon.struct.region.ruled;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * A rule that is bound to a specific region.
 */
public abstract class RegionRule implements Listener, Runnable {
    /**
     * The region this rule is linked to.
     */
    @Getter
    private RuledRegion region;

    /**
     * The task identifier for the bukkit scheduler
     */
    private final int taskId = -1;

    /**
     * Registers this rule under the given region.
     *
     * @param region the region.
     */
    public final void register(RuledRegion region) {
        if (this.region != null)
            throw new IllegalStateException("Cannot register rule twice!");

        this.region = region;
    }

    /**
     * Shuts this region down, unregistering the task and listener.
     */
    public final void shutdown() {
        if (this.region != null)
            throw new IllegalStateException("Region not registered.");

        HandlerList.unregisterAll(this);
        if (this.taskId > 0) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        this.region = null;
    }

    /**
     * Called when enforcing should being. This should be used to clean-up things like unwhitelisted players inside the region.
     */
    public void startEnforcing() {
    }

    /**
     * Called when enforcing has stopped.
     */
    public void stopEnforcing() {
    }

    /**
     * Default implementation of run to allow subclasses to not implement it.
     */
    @Override
    public void run() {
    }

    /**
     * Gets the period of this rule's task. Negative numbers signify no task should be run.
     *
     * @return the task period.
     */
    public int getTaskPeriod() {
        return -1;
    }
}
