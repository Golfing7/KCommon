package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.golfing8.kcommon.util.StringUtil;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.util.List;

/**
 * Gives admins an overview of KCommon and everything running on it.
 * <br>
 * The details are split across the sub commands so a single page of chat stays readable.
 */
@Cmd(
        name = "kstatus",
        aliases = "ks",
        description = "View the status of KCommon"
)
public class KStatusCommand extends KCommand {
    private static final long BYTES_PER_MEGABYTE = 1024L * 1024L;

    @Override
    protected void onRegister() {
        KCommon.getInstance().addLanguageConstant("status-command-overview",
                "&7&m-----&r &aKCommon Status &7&m-----",
                "&aVersion: &e{VERSION} &7(Java {JAVA})",
                "&aServer: &e{SERVER} &7(NMS {NMS}, Folia: {FOLIA}&7)",
                "&aUptime: &e{UPTIME} &7| &aMemory: &e$commas{{MEMORY_USED}}MB&7/&e$commas{{MEMORY_MAX}}MB",
                "&aDebug: {DEBUG} &7| &aTime Zone: &e{TIME_ZONE}",
                "&aEconomy: &e{ECONOMY} &7| &aPlaceholderAPI: {PAPI}",
                "&aMongo: {MONGO}",
                "&aRedis: {REDIS}",
                "&aPlugins: &e{PLUGINS}",
                "&aModules: &e{MODULES_ENABLED}&7/&e{MODULES} &aenabled",
                "&aCommands: &e{COMMANDS} &7({COMMANDS_TOTAL} including sub commands)",
                "&aData Managers: &e{DATA_MANAGERS} &7({DATA_LOCAL} local, {DATA_REMOTE} remote)",
                "&aPlaceholders: &e{PLACEHOLDERS} &7| &aOpen Menus: &e{MENUS}",
                "&7Use &e/kstatus <plugins|modules|commands|data|connections> &7for details."
        );

        addSubCommand(new KStatusPluginsCommand());
        addSubCommand(new KStatusModulesCommand());
        addSubCommand(new KStatusCommandsCommand());
        addSubCommand(new KStatusDataCommand());
        addSubCommand(new KStatusConnectionsCommand());
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        KCommon kCommon = KCommon.getInstance();
        List<Module> modules = KStatusInfo.getModules(null);
        List<KCommand> commands = KStatusInfo.getCommands(null);
        List<KStatusInfo.DataManagerEntry> dataManagers = KStatusInfo.getDataManagers(null);
        List<KPlugin> plugins = KStatusInfo.getPlugins();

        long remoteManagers = dataManagers.stream().filter(KStatusInfo.DataManagerEntry::isRemote).count();
        Runtime runtime = Runtime.getRuntime();
        Economy economy = kCommon.getEconomy();
        int uptimeSeconds = (int) (ManagementFactory.getRuntimeMXBean().getUptime() / 1000L);

        kCommon.sendConfigMessage(context.getSender(), "status-command-overview",
                "VERSION", kCommon.getDescription().getVersion(),
                "JAVA", System.getProperty("java.version"),
                "SERVER", Bukkit.getName() + " " + Bukkit.getBukkitVersion(),
                "NMS", kCommon.getServerVersion(),
                "FOLIA", KStatusInfo.yesNo(FoliaSchedulers.of(kCommon).getFoliaLib().isFolia()),
                "UPTIME", StringUtil.timeFormatted(uptimeSeconds, true),
                "MEMORY_USED", (runtime.totalMemory() - runtime.freeMemory()) / BYTES_PER_MEGABYTE,
                "MEMORY_MAX", runtime.maxMemory() / BYTES_PER_MEGABYTE,
                "DEBUG", KStatusInfo.yesNo(kCommon.isDebug()),
                "TIME_ZONE", kCommon.getTimeZone(),
                "ECONOMY", economy == null ? "none" : economy.getName(),
                "PAPI", KStatusInfo.yesNo(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")),
                "MONGO", KStatusInfo.describeMongo(),
                "REDIS", KStatusInfo.describeRedis(),
                "PLUGINS", plugins.size(),
                "MODULES_ENABLED", modules.stream().filter(Module::isEnabled).count(),
                "MODULES", modules.size(),
                "COMMANDS", commands.size(),
                "COMMANDS_TOTAL", commands.stream().mapToInt(KStatusInfo::countCommandTree).sum(),
                "DATA_MANAGERS", dataManagers.size(),
                "DATA_LOCAL", dataManagers.size() - remoteManagers,
                "DATA_REMOTE", remoteManagers,
                "PLACEHOLDERS", modules.stream().mapToInt(KStatusInfo::countPlaceholders).sum(),
                "MENUS", plugins.stream().mapToInt(plugin -> plugin.getMenuManager().getAll().size()).sum());
    }
}
