package com.golfing8.kcommon;

import com.golfing8.kcommon.command.impl.KModuleCommand;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.library.LibraryDefinition;
import com.golfing8.kcommon.library.LibraryLoader;
import com.golfing8.kcommon.util.NMSVersion;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.Nullable;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * A plugin implementation of {@link KPlugin} so this commons library can be loaded as a standalone.
 */
public class KCommon extends KPlugin{
    @Getter
    private static KCommon instance;
    /** The link to the economy plugin */
    @Getter
    private Economy economy;
    /**
     * The time zone for this plugin to run in
     */
    @Getter
    private ZoneId timeZone;
    @Getter
    private boolean debug;
    /**
     * The version this server is running.
     */
    @Getter
    private NMSVersion serverVersion;

    /** The mongo database connector, can be null if not enabled */
    @Getter
    private @Nullable MongoConnector connector;

    @Override
    public void onPreEnableInner() {
        libraryLoader.addRelocation("de,tr7zw,changeme,nbtapi", "de,tr7zw,kcommon,nbtapi");
        libraryLoader.addRelocation("com,cryptomorin,xseries", "com,golfing8,shade,com,cryptomorin,xseries");

        libraryLoader.loadAllLibraries(Lists.newArrayList(
                new LibraryDefinition("de,tr7zw", "item-nbt-api", "2.12.3", "https://repo.codemc.org/repository/maven-public"),
                new LibraryDefinition("net,objecthunter", "exp4j", "0.4.8"),
                new LibraryDefinition("com,github,cryptomorin", "XSeries", "9.8.1"),
                // For Mongo
                new LibraryDefinition("org,mongodb", "mongodb-driver-core", "5.0.1"),
                new LibraryDefinition("org,mongodb", "mongodb-driver-sync", "5.0.1"),
                new LibraryDefinition("org,mongodb", "bson", "5.0.1")
        ));
    }

    @Override
    public void onEnableInner() {
        instance = this;

        if ((economy = setupEconomy()) == null) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (getConfig().contains("mongo")) {
            if (getConfig().contains("mongo.connection-string") && getConfig().getString("mongo.connection-string").isEmpty()) {
                String username = getConfig().getString("mongo.username");
                String password = getConfig().getString("mongo.password");
                String address = getConfig().getString("mongo.address");
                int port = getConfig().getInt("mongo.port");
                String database = getConfig().getString("mongo.database");
                this.connector = new MongoConnector(username, password, address, port, database);
            } else {
                String connectionString = getConfig().getString("mongo.connection-string");
                String database = getConfig().getString("mongo.database");
                this.connector = new MongoConnector(connectionString, database);
            }
            this.connector.connect();
            getLogger().info("Connected to MongoDB");
        }

        try {
            this.timeZone = ZoneId.of(getConfig().getString("time-zone", "America/New_York"));
        } catch (DateTimeException exc) {
            getLogger().warning(String.format("Failed to load time zone %s. Defaulting to America/New_York", getConfig().getString("time-zone")));
            this.timeZone = ZoneId.of("America/New_York");
        }
        this.debug = getConfig().getBoolean("debug", false);
        this.serverVersion = NMSVersion.fromBukkitPackageName(Bukkit.getServer().getClass().getName().split("\\.")[3]);
        NMS.initialize();
        new KModuleCommand().register();
    }

    private Economy setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            Bukkit.getLogger().severe(String.format("[%s] Disabled due to no Vault dependency found!", getName()));
            return null;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            Bukkit.getLogger().severe(String.format("[%s] Vault found, but there's no economy plugin that uses it!", getName()));
            return null;
        }
        return rsp.getProvider();
    }
}
