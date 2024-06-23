package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.KCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.module.Module;
import org.jetbrains.annotations.NotNull;

@Cmd(
        name = "reload",
        aliases = "r",
        description = "Reload a module"
)
public class KModuleReloadCommand extends KCommand {
    @Override
    protected void onRegister() {
        addArgument("module", CommandArguments.MODULE);
        KCommon.getInstance().addLanguageConstant("reload-command-message", "&aReloaded the &e{MODULE} &amodule in &e{TIME}ms&a.");
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Module module = context.next();
        long timeBegin = System.currentTimeMillis();
        module.reloadWithDependencies();
        long timeEnd = System.currentTimeMillis();

        KCommon.getInstance().sendConfigMessage(context.getSender(), "reload-command-message", "MODULE", module.getModuleName(), "TIME", timeEnd - timeBegin);
    }
}
