package com.golfing8.kcommon.platform;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.util.StringUtilCore;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The Hytale platform.
 * <p>
 * Hytale has no single main thread: every world ticks on its own thread. "Sync" work therefore runs on the
 * default world's thread. Delays and periods stay in Minecraft ticks (50 ms) so configs mean the same thing
 * on both platforms.
 * </p>
 */
public class HytalePlatform extends Platform {
    private static final long MILLIS_PER_TICK = 50;

    private final ExecutorService async = Executors.newCachedThreadPool(runnable -> {
        Thread thread = new Thread(runnable, "KCommon Async");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Stops the async worker threads.
     */
    public void shutdown() {
        async.shutdown();
    }

    private static @Nullable World defaultWorld() {
        Universe universe = Universe.get();
        return universe == null ? null : universe.getDefaultWorld();
    }

    @Override
    public long getCurrentTick() {
        World world = defaultWorld();
        return world == null ? 0 : world.getTick();
    }

    @Override
    public void sendConsoleMessage(String message) {
        getLogger().info(StringUtilCore.stripColor(StringUtilCore.translateAlternateColorCodes('&', message)));
    }

    @Override
    public boolean supportsHexColors() {
        return true;
    }

    @Override
    public Logger getLogger() {
        return KCommon.getInstance().getPluginLogger();
    }

    @Override
    public ZoneId getTimeZone() {
        return KCommon.getInstance().getTimeZone();
    }

    @Override
    public KPluginContext getKCommon() {
        return KCommon.getInstance();
    }

    @Override
    public KPluginContext getProvidingPlugin(Class<?> clazz) {
        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if (plugin instanceof JavaPlugin && plugin instanceof KPluginContext
                    && ((JavaPlugin) plugin).getClassLoader() == clazz.getClassLoader()) {
                return (KPluginContext) plugin;
            }
        }
        throw new IllegalArgumentException(clazz.getName() + " wasn't loaded by a KCommon plugin!");
    }

    @Override
    public @Nullable MongoConnector getMongoConnector() {
        return KCommon.getInstance().getConnector();
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return KCommon.getInstance().getRedisAdapter();
    }

    @Override
    public boolean isMainThread(Thread thread) {
        return false;
    }

    @Override
    public void runSync(Runnable task) {
        World world = defaultWorld();
        if (world == null) {
            task.run();
        } else {
            world.execute(task);
        }
    }

    @Override
    public void runAsync(Runnable task) {
        async.execute(task);
    }

    @Override
    public Terminable runLater(Runnable task, long delayTicks) {
        return cancelling(HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> runSync(task), delayTicks * MILLIS_PER_TICK, TimeUnit.MILLISECONDS));
    }

    @Override
    public Terminable runLaterAsync(Runnable task, long delayTicks) {
        return cancelling(HytaleServer.SCHEDULED_EXECUTOR.schedule(() -> runAsync(task), delayTicks * MILLIS_PER_TICK, TimeUnit.MILLISECONDS));
    }

    @Override
    public Terminable runTimer(Runnable task, long delayTicks, long periodTicks) {
        return cancelling(HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> runSync(task),
                delayTicks * MILLIS_PER_TICK, Math.max(1, periodTicks) * MILLIS_PER_TICK, TimeUnit.MILLISECONDS));
    }

    @Override
    public Terminable runTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        return cancelling(HytaleServer.SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> runAsync(task),
                delayTicks * MILLIS_PER_TICK, Math.max(1, periodTicks) * MILLIS_PER_TICK, TimeUnit.MILLISECONDS));
    }

    private static Terminable cancelling(ScheduledFuture<?> future) {
        return () -> future.cancel(false);
    }
}
