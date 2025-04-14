package com.golfing8.kcommon.struct.map;

import com.google.common.base.Preconditions;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Stores cooldown information for unique IDs.
 */
public class CooldownMap<T> {
    /** The map we store cooldowns in */
    private final Map<T, Long> backingMap;

    /**
     * Creates a new cooldown map with no expiry task.
     */
    public CooldownMap() {
        this.backingMap = new HashMap<>();
    }

    /**
     * Creates a new cooldown map with an expiry task registered to the given plugin.
     *
     * @param plugin the plugin.
     */
    public CooldownMap(Plugin plugin) {
        this();

        new ExpiryTask(this).runTaskTimer(plugin, 0, 20);
    }

    /**
     * Checks if the given the type is on a cooldown.
     *
     * @param id the id.
     * @return if it's on a cooldown.
     */
    public boolean isOnCooldown(@NotNull T id) {
        if (!backingMap.containsKey(id))
            return false;

        if (backingMap.get(id) <= System.currentTimeMillis()) {
            backingMap.remove(id);
            return false;
        }
        return true;
    }

    /**
     * Clears the cooldown on the given id.
     *
     * @param id the id.
     * @return if the uuid was on a cooldown.
     */
    public boolean clearCooldown(@NotNull T id) {
        Long cooldownTime = backingMap.remove(id);
        return cooldownTime != null && cooldownTime > System.currentTimeMillis();
    }

    /**
     * Gets the length, in milliseconds, of the cooldown remaining for the id.
     *
     * @param id the id.
     * @return the cooldown remaining, or 0 if no cooldown is active.
     */
    public long getCooldownRemaining(@NotNull T id) {
        Long remainingTime = backingMap.get(id);
        if (remainingTime == null)
            return 0;

        return Math.max(0, remainingTime - System.currentTimeMillis());
    }

    /**
     * Sets the cooldown on the given id, overriding any pre-existing cooldown.
     *
     * @param id the id.
     * @param durationMillis the duration of the cooldown.
     */
    public void setCooldown(@NotNull T id, long durationMillis) {
        Preconditions.checkArgument(durationMillis >= 0, "Duration must be non-negative");

        backingMap.put(id, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Tries to set the cooldown on the given id.
     * If the id is already on cooldown, the method returns true and nothing changes.
     *
     * @param id the id to put on cooldown
     * @param durationMillis the duration of the cooldown, in millis
     * @return if the player was on cooldown
     * @deprecated return value is inconsistent with expected behavior. {@link #checkAndSetCooldown(T, long)}
     */
    @Deprecated
    public boolean trySetCooldown(@NotNull T id, long durationMillis) {
        if (isOnCooldown(id))
            return true;

        setCooldown(id, durationMillis);
        return false;
    }

    /**
     * Checks if the user is on cooldown and sets them on cooldown if they aren't.
     * Supersedes {@link #trySetCooldown(T, long)}
     *
     * @param id the id.
     * @param durationMillis the duration of their cooldown.
     * @return true if they were set on cooldown, false if they were already on cooldown.
     */
    public boolean checkAndSetCooldown(@NotNull T id, long durationMillis) {
        if (isOnCooldown(id))
            return false;

        setCooldown(id, durationMillis);
        return true;
    }

    /**
     * Purges all cooldowns that have expired.
     */
    public void purgeStaleCooldowns() {
        this.backingMap.entrySet().removeIf(next -> System.currentTimeMillis() >= next.getValue());
    }


    /**
     * This must be kept as a nested static class to allow the CooldownMap instance
     * it's linked to get GCed.
     * TODO Maybe make this async?
     */
    private static class ExpiryTask extends BukkitRunnable {
        /** The cooldown map this task is linked to */
        private final WeakReference<CooldownMap<?>> link;

        public ExpiryTask(CooldownMap<?> link) {
            this.link = new WeakReference<>(link);
        }

        @Override
        public void run() {
            CooldownMap<?> instance = link.get();
            // If instance is null, it has been garbage collected. Don't do anything else with it!
            if (instance == null) {
                cancel();
                return;
            }

            instance.purgeStaleCooldowns();
        }
    }
}
