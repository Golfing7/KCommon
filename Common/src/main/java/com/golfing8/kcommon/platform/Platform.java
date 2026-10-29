package com.golfing8.kcommon.platform;

import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.config.KSection;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.struct.helper.exception.InternalException;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import org.jetbrains.annotations.Nullable;

import java.time.ZoneId;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The few things shared KCommon code needs from the server it runs on.
 * <p>
 * Each platform sets its implementation once while KCommon starts up.
 * </p>
 */
public abstract class Platform {
    private static Platform current;

    /**
     * Gets the platform KCommon is running on.
     *
     * @return the platform.
     */
    public static Platform get() {
        if (current == null)
            throw new IllegalStateException("KCommon has not set up its platform yet!");

        return current;
    }

    /**
     * Sets the platform KCommon is running on.
     *
     * @param platform the platform.
     */
    public static void set(Platform platform) {
        current = platform;
    }

    /**
     * Checks if a platform was set.
     *
     * @return true if a platform was set.
     */
    public static boolean isSet() {
        return current != null;
    }

    /**
     * Gets the current server tick.
     *
     * @return the current tick.
     */
    public abstract long getCurrentTick();

    /**
     * Sends a message to the console. The message may contain '&' color codes.
     *
     * @param message the message.
     */
    public abstract void sendConsoleMessage(String message);

    /**
     * Checks if the server can display hex colors.
     *
     * @return true if hex colors are supported.
     */
    public abstract boolean supportsHexColors();

    /**
     * Gets KCommon's own logger.
     *
     * @return the logger.
     */
    public abstract Logger getLogger();

    /**
     * Gets the time zone KCommon is configured to run in.
     *
     * @return the time zone.
     */
    public abstract ZoneId getTimeZone();

    /**
     * Converts a platform specific config section to a {@link KSection}.
     *
     * @param value the value, which may or may not be a section.
     * @return the section, or null if the value isn't one.
     */
    public @Nullable KSection toSection(Object value) {
        return value instanceof KSection ? (KSection) value : null;
    }

    /**
     * Gets the KCommon plugin itself.
     *
     * @return KCommon.
     */
    public abstract KPluginContext getKCommon();

    /**
     * Gets the KCommon based plugin that loaded the given class.
     *
     * @param clazz the class.
     * @return the plugin.
     */
    public abstract KPluginContext getProvidingPlugin(Class<?> clazz);

    /**
     * Gets the MongoDB connector.
     *
     * @return the connector, or null if MongoDB isn't set up.
     */
    public abstract @Nullable MongoConnector getMongoConnector();

    /**
     * Gets the redis adapter. Without redis this is a no-op adapter.
     *
     * @return the adapter.
     */
    public abstract RedisAdapter getRedisAdapter();

    /**
     * Checks if the given thread is the server's main thread.
     * Servers without a single main thread return false.
     *
     * @param thread the thread.
     * @return true if it's the main thread.
     */
    public abstract boolean isMainThread(Thread thread);

    /**
     * Runs the task on the server's main thread, or right away if already on it.
     *
     * @param task the task.
     */
    public abstract void runSync(Runnable task);

    /**
     * Runs the task off the main thread.
     *
     * @param task the task.
     */
    public abstract void runAsync(Runnable task);

    /**
     * Runs the task on the main thread after the given delay.
     *
     * @param task       the task.
     * @param delayTicks the delay in ticks.
     * @return a handle that cancels the task when closed.
     */
    public abstract Terminable runLater(Runnable task, long delayTicks);

    /**
     * Runs the task off the main thread after the given delay.
     *
     * @param task       the task.
     * @param delayTicks the delay in ticks.
     * @return a handle that cancels the task when closed.
     */
    public abstract Terminable runLaterAsync(Runnable task, long delayTicks);

    /**
     * Repeats the task on the main thread.
     *
     * @param task        the task.
     * @param delayTicks  the delay before the first run, in ticks.
     * @param periodTicks the time between runs, in ticks.
     * @return a handle that cancels the task when closed.
     */
    public abstract Terminable runTimer(Runnable task, long delayTicks, long periodTicks);

    /**
     * Repeats the task off the main thread.
     *
     * @param task        the task.
     * @param delayTicks  the delay before the first run, in ticks.
     * @param periodTicks the time between runs, in ticks.
     * @return a handle that cancels the task when closed.
     */
    public abstract Terminable runTimerAsync(Runnable task, long delayTicks, long periodTicks);

    /**
     * Reports an exception thrown inside a scheduled task, promise or event handler.
     *
     * @param exception the exception.
     */
    public void reportInternalException(InternalException exception) {
        getLogger().log(Level.SEVERE, exception.getMessage(), exception);
    }
}
