package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.command.flag.CommandFlag;
import com.golfing8.kcommon.module.Module;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Shows the state of every module, grouped by the plugin that registered it.
 */
@Cmd(
        name = "modules",
        aliases = "m",
        description = "Shows the state of every module, or those of one plugin"
)
public class KStatusModulesCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("plugin", CommandArguments.KPLUGIN, sender -> "");
        addFlag(new CommandFlag('v', "verbose"));

        KCommon.getInstance().addLanguageConstant("status-command-modules-none",
                "&c{PLUGIN} doesn't have any modules.");
        KCommon.getInstance().addLanguageConstant("status-command-modules-header",
                "&7&m-----&r &a{PLUGIN} &7(&e{ENABLED}&7/&e{MODULES} &aenabled&7) &7&m-----");
        KCommon.getInstance().addLanguageConstant("status-command-modules-format",
                "&7- {MODULE} &7| &e{COMMANDS} &7commands, &e{PLACEHOLDERS} &7placeholders, &e{DATA_MANAGERS} &7data managers");
        KCommon.getInstance().addLanguageConstant("status-command-modules-format-verbose",
                "&7- {MODULE}",
                "   &7Commands: &e{COMMANDS} &7| Placeholders: &e{PLACEHOLDERS} &7| Data Managers: &e{DATA_MANAGERS} &7| Configs: &e{CONFIGS}",
                "   &7Module Dependencies: &e{MODULE_DEPENDENCIES}",
                "   &7Plugin Dependencies: &e{PLUGIN_DEPENDENCIES}");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        KPlugin forPlugin = context.next();
        boolean verbose = context.getFlagState('v') == TriState.TRUE;
        List<Module> modules = KStatusInfo.getModules(forPlugin);
        if (modules.isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-modules-none",
                    "PLUGIN", forPlugin == null ? "Every plugin" : forPlugin.getName());
            return;
        }

        // Modules are already sorted by plugin, so this keeps that order.
        Map<KPlugin, List<Module>> byPlugin = new LinkedHashMap<>();
        for (Module module : modules) {
            byPlugin.computeIfAbsent(module.getPlugin(), k -> new ArrayList<>()).add(module);
        }

        String messageKey = verbose ? "status-command-modules-format-verbose" : "status-command-modules-format";
        for (Map.Entry<KPlugin, List<Module>> entry : byPlugin.entrySet()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "status-command-modules-header",
                    "PLUGIN", entry.getKey().getName(),
                    "ENABLED", entry.getValue().stream().filter(Module::isEnabled).count(),
                    "MODULES", entry.getValue().size());

            for (Module module : entry.getValue()) {
                KCommon.getInstance().sendConfigMessage(context.getSender(), messageKey,
                        "MODULE", describeState(module),
                        "COMMANDS", module.getModuleCommands().size(),
                        "PLACEHOLDERS", KStatusInfo.countPlaceholders(module),
                        "DATA_MANAGERS", KStatusInfo.countDataManagers(module),
                        "CONFIGS", module.getConfigs().size(),
                        "MODULE_DEPENDENCIES", joinOrNone(module.getModuleDependencies()),
                        "PLUGIN_DEPENDENCIES", joinOrNone(module.getPluginDependencies()));
            }
        }
    }

    /**
     * Describes the module's name along with whether it's running.
     * <br>
     * A module that its plugin's manifest wants enabled but isn't running either failed to enable or is missing a
     * dependency, which is the case an admin most wants to spot.
     *
     * @param module the module
     * @return the colored description
     */
    private static String describeState(Module module) {
        if (module.isEnabled())
            return "&a" + module.getModuleName();

        Boolean active = module.getPlugin().getManifest().getModuleStates().get(module.getModuleName());
        if (active == null || active)
            return "&c" + module.getModuleName() + " &c(failed to enable)";
        return "&7" + module.getModuleName() + " &7(disabled)";
    }

    private static String joinOrNone(Collection<String> values) {
        if (values.isEmpty())
            return "none";

        List<String> sorted = new ArrayList<>(values);
        sorted.sort(String.CASE_INSENSITIVE_ORDER);
        return String.join(", ", sorted);
    }
}
