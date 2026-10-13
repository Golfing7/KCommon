package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.command.flag.CommandFlag;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.local.DataManagerLocal;
import com.golfing8.kcommon.data.remote.DataManagerRemote;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.golfing8.kcommon.util.StringUtil;
import net.kyori.adventure.util.TriState;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Lists every data manager, grouped by the plugin it stores data for.
 * <br>
 * Counting what each manager has in storage means reading the disk or the database, so it's only done when asked
 * for and never on the server thread.
 */
@Cmd(
        name = "data",
        aliases = "d",
        description = "Lists the data managers, or those of one plugin"
)
public class KStatusDataCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("plugin", CommandArguments.KPLUGIN, sender -> "");
        addFlag(new CommandFlag('s', "stored"));
        addFlag(new CommandFlag('v', "verbose"));

        KCommon.getInstance().addLanguageConstant("status-command-data-none",
                "&c{PLUGIN} doesn't have any data managers.");
        KCommon.getInstance().addLanguageConstant("status-command-data-counting",
                "&7Counting stored objects, this may take a moment...");
        KCommon.getInstance().addLanguageConstant("status-command-data-header",
                "&7&m-----&r &a{PLUGIN} &7(&e{MANAGERS} &adata managers&7) &7&m-----");
        KCommon.getInstance().addLanguageConstant("status-command-data-format",
                "&7- &e{KEY} &7[{TYPE}&7] &f{CLASS} &7| owner: &e{OWNER} &7| cached: &e$commas{{CACHED}}{STORED}");
        KCommon.getInstance().addLanguageConstant("status-command-data-format-verbose",
                "&7- &e{KEY} &7[{TYPE}&7] &f{CLASS} &7| owner: &e{OWNER} &7| cached: &e$commas{{CACHED}}{STORED}",
                "   &7Location: &f{LOCATION}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        KPlugin forPlugin = context.next();
        boolean stored = context.getFlagState('s') == TriState.TRUE;
        boolean verbose = context.getFlagState('v') == TriState.TRUE;
        CommandSender sender = context.getSender();

        List<KStatusInfo.DataManagerEntry> entries = KStatusInfo.getDataManagers(forPlugin);
        if (entries.isEmpty()) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-data-none",
                    "PLUGIN", forPlugin == null ? "Every plugin" : forPlugin.getName());
            return;
        }

        if (!stored) {
            sendEntries(sender, entries, verbose, null);
            return;
        }

        KCommon.getInstance().sendConfigMessage(sender, "status-command-data-counting");
        FoliaSchedulers.of(KCommon.getInstance()).runAsync(() -> {
            Map<DataManager<?>, String> storedText = new IdentityHashMap<>();
            for (KStatusInfo.DataManagerEntry entry : entries) {
                storedText.put(entry.getManager(), describeStored(entry.getManager()));
            }
            sendEntries(sender, entries, verbose, storedText);
        });
    }

    /**
     * Sends the entries to the sender, grouped by plugin.
     *
     * @param sender     the sender
     * @param entries    the entries, already sorted by plugin
     * @param verbose    if the location of each manager should be shown
     * @param storedText the description of what each manager has stored, or null if it wasn't counted
     */
    private void sendEntries(CommandSender sender, List<KStatusInfo.DataManagerEntry> entries, boolean verbose,
                             @Nullable Map<DataManager<?>, String> storedText) {
        Map<Plugin, List<KStatusInfo.DataManagerEntry>> byPlugin = new LinkedHashMap<>();
        for (KStatusInfo.DataManagerEntry entry : entries) {
            byPlugin.computeIfAbsent(entry.getPlugin(), k -> new ArrayList<>()).add(entry);
        }

        String messageKey = verbose ? "status-command-data-format-verbose" : "status-command-data-format";
        for (Map.Entry<Plugin, List<KStatusInfo.DataManagerEntry>> pluginEntry : byPlugin.entrySet()) {
            KCommon.getInstance().sendConfigMessage(sender, "status-command-data-header",
                    "PLUGIN", pluginEntry.getKey().getName(),
                    "MANAGERS", pluginEntry.getValue().size());

            for (KStatusInfo.DataManagerEntry entry : pluginEntry.getValue()) {
                DataManager<?> manager = entry.getManager();
                KCommon.getInstance().sendConfigMessage(sender, messageKey,
                        "KEY", manager.getKey(),
                        "TYPE", entry.isRemote() ? "&dremote" : "&blocal",
                        "CLASS", manager.getDataClass().getSimpleName(),
                        "OWNER", KStatusInfo.getOwnerName(entry.getOwner()),
                        "CACHED", manager.getAllCached().size(),
                        "STORED", storedText == null ? "" : storedText.get(manager),
                        "LOCATION", describeLocation(manager));
            }
        }
    }

    /**
     * Describes what the manager has in storage. Does I/O.
     *
     * @param manager the manager
     * @return the description, to be appended to the entry's line
     */
    private static String describeStored(DataManager<?> manager) {
        long count = KStatusInfo.countStored(manager);
        if (count < 0)
            return " &7| stored: &cunknown";

        long size = KStatusInfo.getStoredSize(manager);
        return " &7| stored: &e" + StringUtil.parseCommas(count) + (size < 0 ? "" : " &7(" + KStatusInfo.formatBytes(size) + ")");
    }

    /**
     * Describes where the manager keeps its objects.
     *
     * @param manager the manager
     * @return the description
     */
    private static String describeLocation(DataManager<?> manager) {
        if (manager instanceof DataManagerLocal) {
            return ((DataManagerLocal<?>) manager).getDirectoryPrefix().toString();
        } else if (manager instanceof DataManagerRemote) {
            DataManagerRemote<?> remote = (DataManagerRemote<?>) manager;
            return "mongo collection " + remote.getCollection().getNamespace().getCollectionName() +
                    ", redis channel " + remote.getChannel();
        }
        return manager.getClass().getSimpleName();
    }
}
