package com.golfing8.kcommon;

import com.golfing8.kcommon.command.impl.KModuleCommand;
import com.golfing8.kcommon.config.yaml.YamlConfig;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.data.SenderSerializable;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.JedisRedisAdapter;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.hook.protection.OrbisGuardHook;
import com.golfing8.kcommon.hook.protection.ProtectionHook;
import com.golfing8.kcommon.platform.HytalePlatform;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.util.StringUtilCore;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * KCommon as a standalone Hytale plugin. Other plugins depend on {@code Golfing8:KCommon}.
 */
public class KCommon extends KPlugin {
    @Getter
    private static KCommon instance;
    private final HytalePlatform platform = new HytalePlatform();
    /**
     * KCommon's own config.yml.
     */
    @Getter
    private YamlConfig config;
    /**
     * The time zone for this plugin to run in
     */
    @Getter
    private ZoneId timeZone = ZoneId.of("America/New_York");
    @Getter
    private boolean debug;
    /**
     * The mongo database connector, can be null if not enabled
     */
    @Getter
    private @Nullable MongoConnector connector;
    /**
     * The adapter used to signal other server instances about shared data. A no-op adapter without redis.
     */
    @Getter
    private RedisAdapter redisAdapter = RedisAdapter.noop();
    /**
     * The region protection hook. Backed by OrbisGuard when it is installed, otherwise it allows everything.
     */
    @Getter
    private ProtectionHook protectionHook = ProtectionHook.EMPTY;

    public KCommon(JavaPluginInit init) {
        super(init);
        instance = this;
        Platform.set(platform);
    }

    @Override
    public void onSetupInner() {
        this.config = loadConfig();
        try {
            this.timeZone = ZoneId.of(config.getString("time-zone", "America/New_York"));
        } catch (DateTimeException exc) {
            getPluginLogger().warning(String.format("Failed to load time zone %s. Defaulting to America/New_York", config.getString("time-zone")));
        }
        this.debug = config.getBoolean("debug", false);
    }

    @Override
    public void onEnableInner() {
        if (isPluginEnabled("OrbisGuard")) {
            this.protectionHook = new OrbisGuardHook();
            getPluginLogger().info("Hooked into OrbisGuard");
        }

        if (config.contains("mongo") && trySetupMongo()) {
            getPluginLogger().info("Connected to MongoDB");
        }
        if (trySetupRedis()) {
            getPluginLogger().info("Connected to redis");
        }

        new KModuleCommand().register();

        // Cache player data on join and save it on quit, like the Bukkit build does.
        getEventRegistry().registerAsync(PlayerConnectEvent.class, future -> future.thenApply(event -> {
            forEachPlayerManager(manager -> manager.getObject(event.getPlayerRef().getUuid().toString()));
            return event;
        }));
        getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            String playerId = event.getPlayerRef().getUuid().toString();
            forEachPlayerManager(manager -> {
                manager.save(playerId);
                manager.uncache(playerId);
            });
        });
    }

    @Override
    public void onDisableInner() {
        this.redisAdapter.close();
        this.redisAdapter = RedisAdapter.noop();
        platform.shutdown();
    }

    private static void forEachPlayerManager(java.util.function.Consumer<DataManager<?>> action) {
        List<DataManager<?>> managers = new ArrayList<>();
        DataManagerContainer.dataManagers.values().forEach(byKey -> managers.addAll(byKey.values()));
        for (DataManager<?> manager : managers) {
            if (SenderSerializable.class.isAssignableFrom(manager.getDataClass()))
                action.accept(manager);
        }
    }

    private YamlConfig loadConfig() {
        Path path = getDataDirectory().resolve("config.yml");
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                try (InputStream defaults = KCommon.class.getResourceAsStream("/config.yml")) {
                    if (defaults != null)
                        Files.copy(defaults, path);
                    else
                        Files.createFile(path);
                }
            }
        } catch (IOException exc) {
            getPluginLogger().log(Level.SEVERE, "Failed to create config.yml!", exc);
        }

        YamlConfig yamlConfig = new YamlConfig(path);
        yamlConfig.load();
        return yamlConfig;
    }

    private boolean trySetupRedis() {
        if (!config.getBoolean("redis.enabled", false))
            return false;

        JedisRedisAdapter adapter = new JedisRedisAdapter(
                getPluginLogger(),
                config.getString("redis.address", "127.0.0.1"),
                config.getInt("redis.port", 6379),
                config.getString("redis.username", ""),
                config.getString("redis.password", ""),
                config.getInt("redis.database", 0),
                config.getInt("redis.max-connections", 16),
                config.getInt("redis.timeout", 2000));
        try {
            adapter.connect();
        } catch (Exception exc) {
            getPluginLogger().log(Level.WARNING, "Failed to connect to redis. Cross instance signalling is off.", exc);
            adapter.close();
            return false;
        }

        this.redisAdapter = adapter;
        return true;
    }

    private boolean trySetupMongo() {
        String username = config.getString("mongo.username");
        String connectionString = config.getString("mongo.connection-string");
        if (StringUtilCore.isEmpty(username) && StringUtilCore.isEmpty(connectionString))
            return false;

        if (StringUtilCore.isEmpty(connectionString)) {
            String password = config.getString("mongo.password");
            String address = config.getString("mongo.address");
            int port = config.getInt("mongo.port");
            String database = config.getString("mongo.database");
            String authDb = config.getString("mongo.auth-database", database);
            this.connector = new MongoConnector(username, password, address, port, database, authDb);
        } else {
            this.connector = new MongoConnector(connectionString, config.getString("mongo.database"));
        }

        this.connector.connect();
        return true;
    }
}
