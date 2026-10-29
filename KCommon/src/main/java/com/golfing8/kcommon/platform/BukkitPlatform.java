package com.golfing8.kcommon.platform;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.struct.helper.exception.InternalException;
import com.golfing8.kcommon.struct.helper.exception.events.HelperExceptionEvent;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.config.KSection;
import com.golfing8.kcommon.config.commented.BukkitSection;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.logging.Logger;

/**
 * The Bukkit platform.
 */
public class BukkitPlatform extends Platform {
    @Override
    public long getCurrentTick() {
        return NMS.getTheNMS().getCurrentTick();
    }

    @Override
    public void sendConsoleMessage(String message) {
        Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    @Override
    public boolean supportsHexColors() {
        return KCommon.getInstance() == null || KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_16);
    }

    @Override
    public Logger getLogger() {
        return KCommon.getInstance().getLogger();
    }

    @Override
    public ZoneId getTimeZone() {
        return KCommon.getInstance().getTimeZone();
    }

    @Override
    public @Nullable KSection toSection(Object value) {
        return value instanceof ConfigurationSection ? BukkitSection.of((ConfigurationSection) value) : super.toSection(value);
    }

    @Override
    public KPluginContext getKCommon() {
        return KCommon.getInstance();
    }

    @Override
    public KPluginContext getProvidingPlugin(Class<?> clazz) {
        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(clazz);
        return plugin instanceof KPluginContext ? (KPluginContext) plugin : new PluginContext(plugin);
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
        return thread == KCommon.getInstance().getMainThread();
    }

    @Override
    public void runSync(Runnable task) {
        schedulers().runSync(task);
    }

    @Override
    public void runAsync(Runnable task) {
        schedulers().runAsync(task);
    }

    @Override
    public Terminable runLater(Runnable task, long delayTicks) {
        return cancelling(schedulers().runLater(task, delayTicks));
    }

    @Override
    public Terminable runLaterAsync(Runnable task, long delayTicks) {
        return cancelling(schedulers().runLaterAsync(task, delayTicks));
    }

    @Override
    public Terminable runTimer(Runnable task, long delayTicks, long periodTicks) {
        return cancelling(schedulers().runTimer(task, delayTicks, periodTicks));
    }

    @Override
    public Terminable runTimerAsync(Runnable task, long delayTicks, long periodTicks) {
        return cancelling(schedulers().runTimerAsync(task, delayTicks, periodTicks));
    }

    @Override
    public void reportInternalException(InternalException exception) {
        super.reportInternalException(exception);
        Bukkit.getServer().getPluginManager().callEvent(new HelperExceptionEvent(exception));
    }

    private static FoliaSchedulers schedulers() {
        return FoliaSchedulers.of(KCommon.getInstance());
    }

    private static Terminable cancelling(WrappedTask task) {
        return task::cancel;
    }

    /**
     * Exposes a Bukkit plugin that isn't a {@code KPlugin} to shared code.
     */
    private static final class PluginContext implements KPluginContext {
        private final Plugin plugin;

        private PluginContext(Plugin plugin) {
            this.plugin = plugin;
        }

        @Override
        public String getPluginName() {
            return plugin.getName();
        }

        @Override
        public Logger getPluginLogger() {
            return plugin.getLogger();
        }

        @Override
        public Path getPluginDataFolder() {
            return plugin.getDataFolder().toPath();
        }

        @Override
        public Path getPluginJar() {
            try {
                return Paths.get(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            } catch (URISyntaxException exc) {
                throw new IllegalStateException("Couldn't find the jar of " + plugin.getName(), exc);
            }
        }
    }
}
