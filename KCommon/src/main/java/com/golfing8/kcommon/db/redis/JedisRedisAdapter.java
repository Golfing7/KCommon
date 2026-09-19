package com.golfing8.kcommon.db.redis;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link RedisAdapter} backed by jedis.
 * <br>
 * Jedis is never referenced outside of this class, so a server without it on the classpath can still load
 * everything else in KCommon.
 */
public class JedisRedisAdapter implements RedisAdapter {
    /**
     * Releases a lock only if the caller still holds it. Done in one script so the check and the delete can't be
     * split by another instance taking the lock in between them.
     */
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
    /**
     * How long the subscriber thread waits before reconnecting after the connection dropped, in milliseconds.
     */
    private static final long RECONNECT_DELAY = 5000L;
    /**
     * How long to sleep between attempts at taking a lock, in milliseconds.
     */
    private static final long LOCK_RETRY_DELAY = 25L;

    @Getter
    private final String instanceId = UUID.randomUUID().toString();
    private final Logger logger;
    private final String address;
    private final int port;
    private final String username;
    private final String password;
    private final int database;
    private final int maxConnections;
    private final int timeout;
    /**
     * The listeners registered per channel.
     */
    private final Map<String, Set<Consumer<String>>> listeners = new ConcurrentHashMap<>();
    /**
     * Channels asked for while the subscription was still opening. Redis only learns about a channel once the
     * subscription is live, so they wait here until it is.
     */
    private final CopyOnWriteArrayList<String> pendingChannels = new CopyOnWriteArrayList<>();
    /**
     * Publishes messages off of whatever thread asked for them to be published.
     */
    private final ExecutorService publisherService;

    @Getter
    private JedisPool pool;
    /**
     * The thread the subscription is held open on. Subscribing blocks its connection, so it can't share one.
     */
    private Thread subscriberThread;
    private volatile JedisPubSub pubSub;
    private volatile boolean running;

    public JedisRedisAdapter(Logger logger, String address, int port, String username, String password, int database, int maxConnections, int timeout) {
        this.logger = logger;
        this.address = address;
        this.port = port;
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.database = database;
        this.maxConnections = maxConnections;
        this.timeout = timeout;
        this.publisherService = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                .setNameFormat("KCommon Redis Publisher")
                .setDaemon(true)
                .build());
    }

    /**
     * Connects to the redis server and verifies that it answers.
     *
     * @throws IllegalStateException if this adapter is already connected
     */
    public void connect() {
        if (this.pool != null)
            throw new IllegalStateException("Already connected");

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(this.maxConnections);
        poolConfig.setMaxIdle(this.maxConnections);
        poolConfig.setTestOnBorrow(true);

        DefaultJedisClientConfig.Builder clientConfig = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(this.timeout)
                .socketTimeoutMillis(this.timeout)
                .database(this.database);
        if (!this.username.isEmpty())
            clientConfig.user(this.username);
        if (!this.password.isEmpty())
            clientConfig.password(this.password);

        this.pool = new JedisPool(poolConfig, new HostAndPort(this.address, this.port), clientConfig.build());
        try (Jedis jedis = this.pool.getResource()) {
            jedis.ping();
        } catch (Exception exc) {
            this.pool.close();
            this.pool = null;
            throw exc;
        }
        this.running = true;
    }

    @Override
    public boolean isConnected() {
        return this.running && this.pool != null && !this.pool.isClosed();
    }

    @Override
    public void publish(@NotNull String channel, @NotNull String message) {
        if (!isConnected())
            return;

        try (Jedis jedis = this.pool.getResource()) {
            jedis.publish(channel, message);
        }
    }

    @Override
    public void publishAsync(@NotNull String channel, @NotNull String message) {
        if (!isConnected())
            return;

        this.publisherService.execute(() -> {
            try {
                publish(channel, message);
            } catch (Exception exc) {
                this.logger.log(Level.WARNING, String.format("Failed to publish a message on redis channel %s.", channel), exc);
            }
        });
    }

    @Override
    public synchronized void subscribe(@NotNull String channel, @NotNull Consumer<String> listener) {
        boolean newChannel = !this.listeners.containsKey(channel);
        this.listeners.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(listener);
        if (!newChannel || !isConnected())
            return;

        if (this.subscriberThread == null) {
            this.subscriberThread = new Thread(this::runSubscriber, "KCommon Redis Subscriber");
            this.subscriberThread.setDaemon(true);
            this.subscriberThread.start();
            return;
        }

        JedisPubSub active = this.pubSub;
        if (active != null && active.isSubscribed()) {
            active.subscribe(channel);
        } else {
            this.pendingChannels.addIfAbsent(channel);
        }
    }

    /**
     * Subscribes to every channel that was asked for before the subscription was live.
     * <br>
     * Called from the subscriber thread once redis confirms the subscription.
     */
    private synchronized void drainPendingChannels() {
        JedisPubSub active = this.pubSub;
        if (active == null || !active.isSubscribed())
            return;

        for (Iterator<String> iterator = this.pendingChannels.iterator(); iterator.hasNext();) {
            String channel = iterator.next();
            this.pendingChannels.remove(channel);
            if (this.listeners.containsKey(channel))
                active.subscribe(channel);
        }
    }

    @Override
    public synchronized void unsubscribe(@NotNull String channel, @NotNull Consumer<String> listener) {
        Set<Consumer<String>> channelListeners = this.listeners.get(channel);
        if (channelListeners == null)
            return;

        channelListeners.remove(listener);
        if (!channelListeners.isEmpty())
            return;

        this.listeners.remove(channel);
        this.pendingChannels.remove(channel);
        if (this.pubSub != null && this.pubSub.isSubscribed())
            this.pubSub.unsubscribe(channel);
    }

    @Nullable
    @Override
    public String tryLock(@NotNull String key, long timeoutMillis) {
        if (!isConnected())
            return null;

        String token = UUID.randomUUID().toString();
        try (Jedis jedis = this.pool.getResource()) {
            // NX only sets the key if nobody holds it, PX makes a crashed holder release it on its own.
            String result = jedis.set(key, token, SetParams.setParams().nx().px(timeoutMillis));
            return "OK".equals(result) ? token : null;
        }
    }

    @Nullable
    @Override
    public String lock(@NotNull String key, long timeoutMillis, long waitMillis) {
        long deadline = System.currentTimeMillis() + waitMillis;
        do {
            String token = tryLock(key, timeoutMillis);
            if (token != null)
                return token;

            try {
                Thread.sleep(LOCK_RETRY_DELAY);
            } catch (InterruptedException exc) {
                Thread.currentThread().interrupt();
                return null;
            }
        } while (System.currentTimeMillis() < deadline);
        return null;
    }

    @Override
    public boolean unlock(@NotNull String key, @NotNull String token) {
        if (!isConnected())
            return false;

        try (Jedis jedis = this.pool.getResource()) {
            Object result = jedis.eval(UNLOCK_SCRIPT, Collections.singletonList(key), Collections.singletonList(token));
            return result instanceof Long && (Long) result == 1L;
        } catch (Exception exc) {
            this.logger.log(Level.WARNING, String.format("Failed to release the redis lock %s.", key), exc);
            return false;
        }
    }

    @Override
    public void close() {
        this.running = false;
        this.listeners.clear();
        this.pendingChannels.clear();
        if (this.pubSub != null && this.pubSub.isSubscribed()) {
            try {
                this.pubSub.unsubscribe();
            } catch (Exception ignored) {
                // The connection is going away anyway.
            }
        }

        if (this.subscriberThread != null) {
            this.subscriberThread.interrupt();
            this.subscriberThread = null;
        }

        this.publisherService.shutdownNow();
        if (this.pool != null) {
            this.pool.close();
            this.pool = null;
        }
    }

    /**
     * Holds the subscription open, reconnecting whenever redis drops it.
     */
    private void runSubscriber() {
        while (this.running) {
            this.pendingChannels.clear();
            String[] channels = this.listeners.keySet().toArray(new String[0]);
            if (channels.length == 0) {
                if (!sleep(RECONNECT_DELAY))
                    return;
                continue;
            }

            try (Jedis jedis = this.pool.getResource()) {
                this.pubSub = new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        accept(channel, message);
                    }

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        drainPendingChannels();
                    }
                };
                // Blocks this thread until the subscription ends.
                jedis.subscribe(this.pubSub, channels);
            } catch (Exception exc) {
                if (!this.running)
                    return;

                this.logger.log(Level.WARNING, "Lost the redis subscription, reconnecting shortly.", exc);
                if (!sleep(RECONNECT_DELAY))
                    return;
            }
        }
    }

    /**
     * Sleeps for the given time.
     *
     * @param millis the time to sleep for
     * @return false if the thread was interrupted and should stop
     */
    private boolean sleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException exc) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Hands a received message to every listener of its channel.
     *
     * @param channel the channel the message came in on
     * @param message the message
     */
    private void accept(String channel, String message) {
        Set<Consumer<String>> channelListeners = this.listeners.get(channel);
        if (channelListeners == null)
            return;

        for (Consumer<String> listener : channelListeners) {
            try {
                listener.accept(message);
            } catch (Exception exc) {
                this.logger.log(Level.WARNING, String.format("A listener of redis channel %s threw an error.", channel), exc);
            }
        }
    }
}
