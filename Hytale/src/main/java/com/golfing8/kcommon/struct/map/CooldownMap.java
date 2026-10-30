package com.golfing8.kcommon.struct.map;

import com.hypixel.hytale.server.core.HytaleServer;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Stores cooldown information for unique IDs.
 */
public class CooldownMap<T> {
    /**
     * The map we store cooldowns in
     */
    private final ConcurrentHashMap<T, Long> backingMap;

    /**
     * Creates a new cooldown map with an expiry task registered to the given plugin.
     */
    public CooldownMap() {
        this.backingMap = new ConcurrentHashMap<>();
        ExpiryTask expireTask = new ExpiryTask(this);
        expireTask.cancellableTask = HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(expireTask, 0, 10, TimeUnit.SECONDS);
    }

    /**
     * Checks if the given the type is on a cooldown.
     *
     * @param id the id.
     * @return if it's on a cooldown.
     */
    public boolean isOnCooldown(T id) {
        // Single read: ExpiryTask may purge the entry concurrently
        Long expiry = backingMap.get(id);
        if (expiry == null)
            return false;

        if (expiry <= System.currentTimeMillis()) {
            backingMap.remove(id, expiry);
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
    public boolean clearCooldown(T id) {
        Long cooldownTime = backingMap.remove(id);
        return cooldownTime != null && cooldownTime > System.currentTimeMillis();
    }

    /**
     * Gets the length, in milliseconds, of the cooldown remaining for the id.
     *
     * @param id the id.
     * @return the cooldown remaining, or 0 if no cooldown is active.
     */
    public long getCooldownRemaining(T id) {
        Long remainingTime = backingMap.get(id);
        if (remainingTime == null)
            return 0;

        return Math.max(0, remainingTime - System.currentTimeMillis());
    }

    /**
     * Sets the cooldown on the given id, overriding any pre-existing cooldown.
     *
     * @param id             the id.
     * @param durationMillis the duration of the cooldown.
     */
    public void setCooldown(T id, long durationMillis) {
        backingMap.put(id, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Tries to set the cooldown on the given id.
     * If the id is already on cooldown, the method returns true and nothing changes.
     *
     * @param id             the id to put on cooldown
     * @param durationMillis the duration of the cooldown, in millis
     * @return if the player was on cooldown
     * @deprecated return value is inconsistent with expected behavior. {@link #checkAndSetCooldown(Object, long)}
     */
    @Deprecated
    public boolean trySetCooldown(T id, long durationMillis) {
        if (isOnCooldown(id))
            return true;

        setCooldown(id, durationMillis);
        return false;
    }

    /**
     * Checks if the user is on cooldown and sets them on cooldown if they aren't.
     * Supersedes {@link #trySetCooldown(Object, long)}
     *
     * @param id             the id.
     * @param durationMillis the duration of their cooldown.
     * @return true if they were set on cooldown, false if they were already on cooldown.
     */
    public boolean checkAndSetCooldown(T id, long durationMillis) {
        long now = System.currentTimeMillis();
        boolean[] set = {false};
        // Atomic so two concurrent callers cannot both pass
        backingMap.compute(id, (k, expiry) -> {
            if (expiry != null && expiry > now)
                return expiry;
            set[0] = true;
            return now + durationMillis;
        });
        return set[0];
    }

    /**
     * Purges all cooldowns that have expired.
     */
    public void purgeStaleCooldowns() {
        this.backingMap.entrySet().removeIf(next -> System.currentTimeMillis() >= next.getValue());
    }

    /**
     * Gets the cooldowns in this map
     *
     * @return the cooldowns
     */
    public Map<T, Long> getCooldowns() {
        this.purgeStaleCooldowns();
        return Collections.unmodifiableMap(this.backingMap);
    }

    /**
     * Clears everything tracked by this map
     */
    public void clear() {
        this.backingMap.clear();
    }

    /**
     * This must be kept as a nested static class to allow the CooldownMap instance
     * it's linked to get GCed.
     */
    private static class ExpiryTask implements Runnable {
        /**
         * The cooldown map this task is linked to
         */
        private final WeakReference<CooldownMap<?>> link;
        public ScheduledFuture<?> cancellableTask;

        ExpiryTask(CooldownMap<?> link) {
            this.link = new WeakReference<>(link);
        }

        @Override
        public void run() {
            CooldownMap<?> instance = link.get();
            // If instance is null, it has been garbage collected. Don't do anything else with it!
            if (instance == null) {
                cancellableTask.cancel(true);
                return;
            }

            instance.purgeStaleCooldowns();
        }
    }
}
