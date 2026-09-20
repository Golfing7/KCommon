package com.golfing8.kcommon.db.redis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An adapter over a redis server, used to signal other instances of a plugin about shared data.
 * <br>
 * KCommon's data is cached per JVM. When the same database is shared by more than one server, an instance has no
 * way of knowing that another one changed an object out from under it. This adapter covers the two things needed
 * to work around that: a channel to announce changes on and a lock to serialize changes with.
 * <br>
 * Redis is entirely optional. When it isn't configured, {@link #noop()} is used instead and everything behaves
 * exactly like a single server install, so nothing that worked before needs to change.
 */
public interface RedisAdapter extends Closeable {
    /**
     * The default time a lock is held before redis expires it, in milliseconds.
     */
    long DEFAULT_LOCK_TIMEOUT = 5000L;
    /**
     * The default time spent waiting for a lock before giving up, in milliseconds.
     */
    long DEFAULT_LOCK_WAIT = 1000L;

    /**
     * Gets the adapter used when redis isn't available.
     *
     * @return the no-op adapter
     */
    static RedisAdapter noop() {
        return NoOpRedisAdapter.INSTANCE;
    }

    /**
     * Gets the ID identifying this server instance on the network.
     * <br>
     * Messages carry the ID of the instance that sent them so an instance can ignore its own broadcasts.
     *
     * @return the instance ID
     */
    @NotNull
    String getInstanceId();

    /**
     * If this adapter is backed by a live redis connection.
     *
     * @return true if connected, false if this adapter does nothing
     */
    boolean isConnected();

    /**
     * Publishes a message on the given channel, blocking until redis accepts it.
     *
     * @param channel the channel
     * @param message the message
     */
    void publish(@NotNull String channel, @NotNull String message);

    /**
     * Publishes a message on the given channel without blocking the calling thread.
     * <br>
     * Use this when publishing from the server thread. Failures are logged instead of thrown.
     *
     * @param channel the channel
     * @param message the message
     */
    void publishAsync(@NotNull String channel, @NotNull String message);

    /**
     * Registers a listener for messages published on the given channel by other instances.
     * <br>
     * Listeners are called on the adapter's subscriber thread, never on the server thread.
     *
     * @param channel  the channel
     * @param listener the listener
     */
    void subscribe(@NotNull String channel, @NotNull Consumer<String> listener);

    /**
     * Unregisters a listener previously passed to {@link #subscribe(String, Consumer)}.
     *
     * @param channel  the channel
     * @param listener the listener
     */
    void unsubscribe(@NotNull String channel, @NotNull Consumer<String> listener);

    /**
     * Tries to take the lock with the given key once.
     *
     * @param key           the key of the lock
     * @param timeoutMillis how long redis holds the lock before expiring it
     * @return the token identifying this holder, or null if another instance holds the lock
     */
    @Nullable
    String tryLock(@NotNull String key, long timeoutMillis);

    /**
     * Tries to take the lock with the given key, retrying until the wait time runs out.
     *
     * @param key           the key of the lock
     * @param timeoutMillis how long redis holds the lock before expiring it
     * @param waitMillis    how long to keep retrying for
     * @return the token identifying this holder, or null if the lock couldn't be taken in time
     */
    @Nullable
    String lock(@NotNull String key, long timeoutMillis, long waitMillis);

    /**
     * Releases the lock with the given key.
     * <br>
     * The token is compared against the one held in redis, so an instance whose lock already expired cannot
     * release the lock that another instance has since taken.
     *
     * @param key   the key of the lock
     * @param token the token returned when the lock was taken
     * @return true if this holder still held the lock
     */
    boolean unlock(@NotNull String key, @NotNull String token);

    /**
     * Runs the given supplier while holding the lock with the given key.
     *
     * @param key           the key of the lock
     * @param timeoutMillis how long redis holds the lock before expiring it
     * @param waitMillis    how long to keep retrying for
     * @param supplier      the supplier to run
     * @param <R>           the type returned by the supplier
     * @return whatever the supplier returned
     * @throws IllegalStateException if the lock couldn't be taken in time
     */
    default <R> R withLock(@NotNull String key, long timeoutMillis, long waitMillis, @NotNull Supplier<R> supplier) {
        String token = lock(key, timeoutMillis, waitMillis);
        if (token == null)
            throw new IllegalStateException(String.format("Failed to acquire the redis lock %s.", key));

        try {
            return supplier.get();
        } finally {
            unlock(key, token);
        }
    }

    /**
     * Closes this adapter and everything it owns.
     */
    @Override
    void close();
}
