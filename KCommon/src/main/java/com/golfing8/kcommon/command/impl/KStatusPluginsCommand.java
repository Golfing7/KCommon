package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.module.Module;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lists every plugin built on KCommon along with what each one has registered.
 */
@Cmd(
        name = "plugins",
        aliases = "p",
        description = "Lists the plugins running on KCommon"
)
public class KStatusPluginsCommand extends KCommand {
    @Override
    protected void onRegister() {
        KCommon.getInstance().addLanguageConstant("status-command-plugins-header",
                "&7&m-----&r &aKCommon Plugins &7(&e{PLUGINS}&7) &7&m-----");
        KCommon.getInstance().addLanguageConstant("status-command-plugins-format",
                "&e{PLUGIN} &7v{VERSION}",
                " &7- &aModules: &e{MODULES_ENABLED}&7/&e{MODULES} &aenabled",
                " &7- &aCommands: &e{COMMANDS} &7({COMMANDS_TOTAL} including sub commands)",
                " &7- &aData Managers: &e{DATA_MANAGERS} &7({DATA_REMOTE} remote)",
                " &7- &aPlaceholders: &e{PLACEHOLDERS} &7| &aOpen Menus: &e{MENUS}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        List<KPlugin> plugins = KStatusInfo.getPlugins();
        KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-plugins-header",
                "PLUGINS", plugins.size());

        for (KPlugin plugin : plugins) {
            List<Module> modules = KStatusInfo.getModules(plugin);
            List<KCommand> commands = KStatusInfo.getCommands(plugin);
            List<KStatusInfo.DataManagerEntry> dataManagers = KStatusInfo.getDataManagers(plugin);

            KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-plugins-format",
                    "PLUGIN", plugin.getName(),
                    "VERSION", plugin.getDescription().getVersion(),
                    "MODULES_ENABLED", modules.stream().filter(Module::isEnabled).count(),
                    "MODULES", modules.size(),
                    "COMMANDS", commands.size(),
                    "COMMANDS_TOTAL", commands.stream().mapToInt(KStatusInfo::countCommandTree).sum(),
                    "DATA_MANAGERS", dataManagers.size(),
                    "DATA_REMOTE", dataManagers.stream().filter(KStatusInfo.DataManagerEntry::isRemote).count(),
                    "PLACEHOLDERS", modules.stream().mapToInt(KStatusInfo::countPlaceholders).sum(),
                    "MENUS", plugin.getMenuManager().getAll().size());
        }
    }
}
