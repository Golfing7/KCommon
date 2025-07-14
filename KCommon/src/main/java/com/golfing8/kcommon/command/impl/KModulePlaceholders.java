package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;
import lombok.var;
import org.jetbrains.annotations.NotNull;

/**
 * Lets you list placeholders for a module
 */
@Cmd(
        name = "placeholders",
        aliases = "placeholder",
        description = "See available placeholders"
)
public class KModulePlaceholders extends KCommand {
    @Override
    protected void onRegister() {
        KCommon.getInstance().getLangConfig().addLanguageConstant("placeholders-command-none",
                "&c{MODULE} doesn't have any placeholders.");
        KCommon.getInstance().getLangConfig().addLanguageConstant("placeholders-command-header",
                "&7&m-----&r &a{MODULE} placeholders &7&m-----");
        KCommon.getInstance().getLangConfig().addLanguageConstant("placeholders-command-format-simple",
                "&e%{PLACEHOLDER}% &7- &a{DESCRIPTION}");
        KCommon.getInstance().getLangConfig().addLanguageConstant("placeholders-command-format-relational",
                "&d%{PLACEHOLDER}% &7- &a{DESCRIPTION} (Rel)");

        addArgument("module", CommandArguments.MODULE);
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Module module = context.next();
        if (module.getPlaceholders().isEmpty() && module.getRelationalPlaceholders().isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "placeholders-command-none", "MODULE", module.getModuleName());
            return;
        }

        KCommon.getInstance().sendConfigMessage(context.getSender(), "placeholders-command-header", "MODULE", module.getModuleName());
        for (var key : module.getPlaceholders().keySet()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "placeholders-command-format-simple",
                    "PLACEHOLDER", key.formatLabel(module),
                    "DESCRIPTION", key.getDescription());
        }
        for (var key : module.getRelationalPlaceholders().keySet()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "placeholders-command-format-relational",
                    "PLACEHOLDER", "rel_" + key.formatLabel(module),
                    "DESCRIPTION", key.getDescription());
        }
    }
}
