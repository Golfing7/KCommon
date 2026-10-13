package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.flag.CommandFlag;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.mongodb.connection.ServerDescription;
import net.kyori.adventure.util.TriState;
import org.bson.Document;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Shows the state of the connections to mongo and redis.
 * <br>
 * Both servers are pinged to make sure they actually answer, which is done off of the server thread.
 */
@Cmd(
        name = "connections",
        aliases = {"db", "conn"},
        description = "Shows the state of the mongo and redis connections"
)
public class KStatusConnectionsCommand extends KCommand {
    @Override
    protected void onRegister() {
        addFlag(new CommandFlag('v', "verbose"));

        KCommon.getInstance().addLanguageConstant("status-command-connections-mongo-none",
                "&7&m-----&r &aMongoDB &7&m-----",
                "&aStatus: &7not configured",
                "&7Set the &emongo &7section in KCommon's config.yml to store data remotely.");
        KCommon.getInstance().addLanguageConstant("status-command-connections-mongo",
                "&7&m-----&r &aMongoDB &7&m-----",
                "&aStatus: {STATUS}",
                "&aDatabase: &e{DATABASE} &7| &aCluster Type: &e{CLUSTER_TYPE}",
                "&aPing: {PING}",
                "&aRemote Data Managers: &e{REMOTE_MANAGERS}",
                "&aServers: &e{SERVERS_UP}&7/&e{SERVERS}");
        KCommon.getInstance().addLanguageConstant("status-command-connections-mongo-server",
                " &7- &e{ADDRESS} &7[{STATE}&7] type: &e{TYPE} &7| round trip: &e{ROUND_TRIP}ms");
        KCommon.getInstance().addLanguageConstant("status-command-connections-redis-disabled",
                "&7&m-----&r &aRedis &7&m-----",
                "&aStatus: &7disabled",
                "&7Turn on &eredis.enabled &7in KCommon's config.yml when servers share a mongo database.");
        KCommon.getInstance().addLanguageConstant("status-command-connections-redis-failed",
                "&7&m-----&r &aRedis &7&m-----",
                "&aStatus: &cnot connected",
                "&aConfigured Address: &e{ADDRESS}",
                "&7Redis is enabled but the connection failed, data managers can't signal other servers.",
                "&7Check the console from startup for the error.");
        KCommon.getInstance().addLanguageConstant("status-command-connections-redis",
                "&7&m-----&r &aRedis &7&m-----",
                "&aStatus: {STATUS}",
                "&aAddress: &e{ADDRESS}",
                "&aInstance ID: &e{INSTANCE}",
                "&aPing: {PING}",
                "&aPool: &e{ACTIVE} &7active, &e{IDLE} &7idle",
                "&aChannels: &e{CHANNELS}");
        KCommon.getInstance().addLanguageConstant("status-command-connections-redis-channel",
                " &7- &e{CHANNEL}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        boolean verbose = context.getFlagState('v') == TriState.TRUE;
        CommandSender sender = context.getSender();
        // The data manager maps are only safe to read from the server thread, so count them before going async.
        long remoteManagers = KStatusInfo.getDataManagers(null).stream()
                .filter(KStatusInfo.DataManagerEntry::isRemote)
                .count();

        FoliaSchedulers.of(KCommon.getInstance()).runAsync(() -> {
            sendMongo(sender, remoteManagers);
            sendRedis(sender, verbose);
        });
    }

    private void sendMongo(CommandSender sender, long remoteManagers) {
        MongoConnector connector = KCommon.getInstance().getConnector();
        if (connector == null || connector.getClient() == null) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-mongo-none");
            return;
        }

        List<ServerDescription> servers = KStatusInfo.getMongoServers();
        long serversUp = servers.stream().filter(ServerDescription::isOk).count();

        // Without a server that's up the ping would block until the driver gives up selecting one, which takes ages.
        String ping = serversUp > 0 ? pingMongo(connector) : "&cskipped, no servers are up";
        KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-mongo",
                "STATUS", serversUp > 0 ? "&aconnected" : "&cunreachable",
                "DATABASE", connector.getDatabase().getName(),
                "CLUSTER_TYPE", connector.getClient().getClusterDescription().getType(),
                "PING", ping,
                "REMOTE_MANAGERS", remoteManagers,
                "SERVERS_UP", serversUp,
                "SERVERS", servers.size());

        for (ServerDescription server : servers) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-mongo-server",
                    "ADDRESS", server.getAddress(),
                    "STATE", server.isOk() ? "&a" + server.getState() : "&c" + server.getState(),
                    "TYPE", server.getType(),
                    "ROUND_TRIP", TimeUnit.NANOSECONDS.toMillis(server.getRoundTripTimeNanos()));
        }
    }

    private static String pingMongo(MongoConnector connector) {
        long start = System.nanoTime();
        try {
            connector.getDatabase().runCommand(new Document("ping", 1));
            return "&e" + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start) + "ms";
        } catch (Exception exc) {
            return "&cfailed &7(" + exc.getClass().getSimpleName() + ")";
        }
    }

    private void sendRedis(CommandSender sender, boolean verbose) {
        if (!KStatusInfo.isRedisEnabled()) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-redis-disabled");
            return;
        }

        RedisAdapter adapter = KCommon.getInstance().getRedisAdapter();
        if (!adapter.isConnected()) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-redis-failed",
                    "ADDRESS", KCommon.getInstance().getConfig().getString("redis.address", "127.0.0.1") + ":" +
                            KCommon.getInstance().getConfig().getInt("redis.port", 6379));
            return;
        }

        long ping = adapter.ping();
        List<String> channels = new ArrayList<>(adapter.getChannels());
        channels.sort(String.CASE_INSENSITIVE_ORDER);
        KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-redis",
                "STATUS", ping >= 0 ? "&aconnected" : "&cunreachable",
                "ADDRESS", adapter.getAddress(),
                "INSTANCE", adapter.getInstanceId(),
                "PING", ping >= 0 ? "&e" + ping + "ms" : "&cfailed",
                "ACTIVE", adapter.getActiveConnections(),
                "IDLE", adapter.getIdleConnections(),
                "CHANNELS", channels.size());

        if (!verbose)
            return;

        for (String channel : channels) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-connections-redis-channel",
                    "CHANNEL", channel);
        }
    }
}
