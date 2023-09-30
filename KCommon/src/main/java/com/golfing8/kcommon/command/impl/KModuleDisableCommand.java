package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;

@Cmd(
        name = "disable",
        permission = "kcommon.command.kmodules.disable",
        description = "Disables a module"
)
public class KModuleDisableCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("module", CommandArguments.MODULE);
        KCommon.getInstance().addLanguageConstant("disable-command-message", "&cDisabled &e{MODULE} &cin &e{TIME}ms&c.");
        KCommon.getInstance().addLanguageConstant("disable-command-already", "&cThat module is already disabled.");
    }

    @Override
    protected void execute(CommandContext context) {
        Module module = context.next();
        if (!module.getPlugin().getManifest().isActive(module) && !module.isEnabled()) {
            KCommon.getInstance().sendConfigMessage(context.getSender(), "disable-command-already");
            return;
        }

        long tNow = System.currentTimeMillis();
        module.getPlugin().getManifest().setActive(module, false);
        module.disable();
        long tEnd = System.currentTimeMillis();
        KCommon.getInstance().sendConfigMessage(context.getSender(), "disable-command-message",
                "MODULE", module.getModuleName(),
                "TIME", tEnd - tNow);
    }
}
