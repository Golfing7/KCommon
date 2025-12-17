package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;
import org.jetbrains.annotations.NotNull;

/**
 * Enables a module
 */
@Cmd(
        name = "enable",
        description = "Enables a module"
)
public class KModuleEnableCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("module", CommandArguments.MODULE);
        KCommon.getInstance().addLanguageConstant("enable-command-message", "&aEnabled &e{MODULE} &ain &e{TIME}ms&a.");
        KCommon.getInstance().addLanguageConstant("enable-command-already", "&cThat module is already enabled.");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Module module = context.next();
        if (module.getPlugin().getManifest().isActive(module) && module.isEnabled()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "enable-command-already");
            return;
        }

        long tNow = System.currentTimeMillis();
        module.getPlugin().getManifest().setActive(module, true);
        module.enable();
        long tEnd = System.currentTimeMillis();
        KCommon.getInstance().sendConfigMessage(context.getSender(), "enable-command-message",
                "MODULE", module.getModuleName(),
                "TIME", tEnd - tNow);
    }
}
