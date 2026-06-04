package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * A command that lets you list the registered commands to a module.
 */
@Cmd(
        name = "commands",
        aliases = "cmd",
        description = "Lists registered commands to a module"
)
public class KModuleCommandsCommand extends KCommand {
    @Override
    protected void onRegister() {
        KCommon.getInstance().getLangConfig().addLanguageConstant("commands-command-none",
                "&c{MODULE} doesn't have any commands.");
        KCommon.getInstance().getLangConfig().addLanguageConstant("commands-command-header",
                "&e{MODULE} &ahas &e{COMMANDS} &acommands!");

        addArgument("module", CommandArguments.MODULE);
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Module module = context.next();

        if (module.getModuleCommands().isEmpty()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "commands-command-none",
                    "MODULE", module.getModuleName());
            return;
        }

        KCommon.getInstance().sendConfigMessage(context.getSender(), "commands-command-header",
                "MODULE", module.getModuleName(),
                "COMMANDS", module.getModuleCommands().size());
        for (MCommand<?> mCommand : module.getModuleCommands()) {
            mCommand.handleHelpMessage(context.getSender(), null, 1);
        }
    }
}
