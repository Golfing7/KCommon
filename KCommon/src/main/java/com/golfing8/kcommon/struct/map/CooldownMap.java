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
public class CooldownMap {
    /** The map we store cooldowns in */
    private final Map<UUID, Long> backingMap;

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
     * Checks if the given UUID is on a cooldown.
     *
     * @param uuid the uuid.
     * @return if it's on a cooldown.
     */
    public boolean isOnCooldown(@NotNull UUID uuid) {
        if (!backingMap.containsKey(uuid))
            return false;

        if (backingMap.get(uuid) <= System.currentTimeMillis()) {
            backingMap.remove(uuid);
            return false;
        }
        return true;
    }

    /**
     * Clears the cooldown on the given UUID.
     *
     * @param uuid the uuid.
     * @return if the uuid was on a cooldown.
     */
    public boolean clearCooldown(@NotNull UUID uuid) {
        Long cooldownTime = backingMap.remove(uuid);
        return cooldownTime != null && cooldownTime > System.currentTimeMillis();
    }

    /**
     * Gets the length, in milliseconds, of the cooldown remaining for the uuid.
     *
     * @param uuid the uuid.
     * @return the cooldown remaining, or 0 if no cooldown is active.
     */
    public long getCooldownRemaining(@NotNull UUID uuid) {
        Long remainingTime = backingMap.get(uuid);
        if (remainingTime == null)
            return 0;

        return Math.max(0, remainingTime - System.currentTimeMillis());
    }

    /**
     * Sets the cooldown on the given UUID, overriding any pre-existing cooldown.
     *
     * @param uuid the UUID.
     * @param durationMillis the duration of the cooldown.
     */
    public void setCooldown(@NotNull UUID uuid, long durationMillis) {
        Preconditions.checkArgument(durationMillis >= 0, "Duration must be non-negative");

        backingMap.put(uuid, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Tries to set the cooldown on the given UUID.
     * If the UUID is already on cooldown, the method returns true and nothing changes.
     *
     * @param uuid the uuid to put on cooldown
     * @param durationMillis the duration of the cooldown, in millis
     * @return if the player was on cooldown
     */
    public boolean trySetCooldown(@NotNull UUID uuid, long durationMillis) {
        if (isOnCooldown(uuid))
            return true;

        setCooldown(uuid, durationMillis);
        return false;
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
        private final WeakReference<CooldownMap> link;

        public ExpiryTask(CooldownMap link) {
            this.link = new WeakReference<>(link);
        }

        @Override
        public void run() {
            CooldownMap instance = link.get();
            // If instance is null, it has been garbage collected. Don't do anything else with it!
            if (instance == null) {
                cancel();
                return;
            }

            instance.purgeStaleCooldowns();
        }
    }
}
