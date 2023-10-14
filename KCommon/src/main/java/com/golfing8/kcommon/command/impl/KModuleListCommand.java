package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;

import java.util.List;
import java.util.stream.Collectors;

@Cmd(
        name = "list",
        aliases = "l",
        permission = "kcommon.command.kmodules.list",
        description = "List all modules globally, or for a plugin"
)
public class KModuleListCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("plugin", CommandArguments.KPLUGIN, (sender) -> "");

        KCommon.getInstance().getLangConfig().addLanguageConstant("list-command-none",
                "&c{PLUGIN} doesn't have any modules.");
        KCommon.getInstance().getLangConfig().addLanguageConstant("list-command-header",
                "&7&m----- &a{PLUGIN} modules &7&m-----");
        KCommon.getInstance().getLangConfig().addLanguageConstant("list-command-format",
                "&7- {MODULE}");
    }

    @Override
    protected void execute(CommandContext context) {
        KPlugin forPlugin = context.next();
        List<Module> modules = Modules.getAll().stream()
                .filter(mod -> forPlugin == null || mod.getPlugin() == forPlugin)
                .sorted((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.getModuleName(), m2.getModuleName()))
                .collect(Collectors.toList());

        if (modules.isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "list-command-none",
                    "PLUGIN", forPlugin == null ? "Every plugin" : forPlugin.getName());
            return;
        }

        KCommon.getInstance().sendConfigMessage(context.getSender(), "list-command-header",
                "PLUGIN", forPlugin == null ? "All" : forPlugin.getName());

        for (Module module : modules) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "list-command-format",
                    "MODULE", (module.isEnabled() ? "&a" : "&c") + module.getModuleName());
        }
    }
}
