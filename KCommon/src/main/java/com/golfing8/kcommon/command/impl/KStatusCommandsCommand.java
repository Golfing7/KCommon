package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.command.flag.CommandFlag;
import net.kyori.adventure.util.TriState;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lists every command registered through KCommon, grouped by the plugin that registered it.
 */
@Cmd(
        name = "commands",
        aliases = "c",
        description = "Lists the commands registered through KCommon, or those of one plugin"
)
public class KStatusCommandsCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("plugin", CommandArguments.KPLUGIN, sender -> "");
        addFlag(new CommandFlag('v', "verbose"));

        KCommon.getInstance().addLanguageConstant("status-command-commands-none",
                "&c{PLUGIN} doesn't have any commands.");
        KCommon.getInstance().addLanguageConstant("status-command-commands-header",
                "&7&m-----&r &a{PLUGIN} &7(&e{COMMANDS} &acommands, &e{COMMANDS_TOTAL} &awith sub commands&7) &7&m-----");
        KCommon.getInstance().addLanguageConstant("status-command-commands-format",
                "&7- &e/{COMMAND}{ALIASES} &7| &e{SUB_COMMANDS} &7sub commands &7| {OWNER}");
        KCommon.getInstance().addLanguageConstant("status-command-commands-format-verbose",
                "&7- &e/{COMMAND}{ALIASES} &7| &e{SUB_COMMANDS} &7sub commands &7| {OWNER}",
                "   &7Description: &f{DESCRIPTION}",
                "   &7Permission: &f{PERMISSION} &7| Players only: {PLAYERS_ONLY} &7| Async: {ASYNC}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        KPlugin forPlugin = context.next();
        boolean verbose = context.getFlagState('v') == TriState.TRUE;
        List<KCommand> commands = KStatusInfo.getCommands(forPlugin);
        if (commands.isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-commands-none",
                    "PLUGIN", forPlugin == null ? "Every plugin" : forPlugin.getName());
            return;
        }

        // Commands are already sorted by plugin, so this keeps that order.
        Map<Plugin, List<KCommand>> byPlugin = new LinkedHashMap<>();
        for (KCommand command : commands) {
            byPlugin.computeIfAbsent(command.getPlugin(), k -> new ArrayList<>()).add(command);
        }

        String messageKey = verbose ? "status-command-commands-format-verbose" : "status-command-commands-format";
        for (Map.Entry<Plugin, List<KCommand>> entry : byPlugin.entrySet()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-commands-header",
                    "PLUGIN", entry.getKey().getName(),
                    "COMMANDS", entry.getValue().size(),
                    "COMMANDS_TOTAL", entry.getValue().stream().mapToInt(KStatusInfo::countCommandTree).sum());

            for (KCommand command : entry.getValue()) {
                KCommon.getInstance().sendConfigMessage(context.getSender(), messageKey,
                        "COMMAND", command.getCommandName(),
                        "ALIASES", command.getCommandAliases().isEmpty() ? "" : " &7(" + String.join(", ", command.getCommandAliases()) + ")",
                        "SUB_COMMANDS", KStatusInfo.countCommandTree(command) - 1,
                        "OWNER", command instanceof MCommand ? "&amodule " + ((MCommand<?>) command).getModule().getModuleName() : "&bplugin",
                        "DESCRIPTION", command.getDescription(),
                        "PERMISSION", command.getCommandPermission(),
                        "PLAYERS_ONLY", KStatusInfo.yesNo(command.isOnlyForPlayers()),
                        "ASYNC", KStatusInfo.yesNo(command.isAsync()));
            }
        }
    }
}
