package com.golfing8.kcommon.db.redis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * The adapter used when redis isn't configured or jedis isn't on the classpath.
 * <br>
 * Nothing is published, nothing is received, and every lock is granted immediately. A single server has nobody to
 * race against, so callers can use the adapter unconditionally and get the old, redis-less behaviour.
 */
public final class NoOpRedisAdapter implements RedisAdapter {
    /**
     * The only instance of this adapter.
     */
    public static final NoOpRedisAdapter INSTANCE = new NoOpRedisAdapter();
    /**
     * The token handed out for every lock. Nothing else can hold the lock, so one token is enough.
     */
    private static final String TOKEN = "local";

    private final String instanceId = UUID.randomUUID().toString();

    private NoOpRedisAdapter() {
    }

    @NotNull
    @Override
    public String getInstanceId() {
        return this.instanceId;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void publish(@NotNull String channel, @NotNull String message) {
    }

    @Override
    public void publishAsync(@NotNull String channel, @NotNull String message) {
    }

    @Override
    public void subscribe(@NotNull String channel, @NotNull Consumer<String> listener) {
    }

    @Override
    public void unsubscribe(@NotNull String channel, @NotNull Consumer<String> listener) {
    }

    @Nullable
    @Override
    public String tryLock(@NotNull String key, long timeoutMillis) {
        return TOKEN;
    }

    @Nullable
    @Override
    public String lock(@NotNull String key, long timeoutMillis, long waitMillis) {
        return TOKEN;
    }

    @Override
    public boolean unlock(@NotNull String key, @NotNull String token) {
        return true;
    }

    @Override
    public void close() {
    }
}
