package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.CommandVisibility;
import com.golfing8.kcommon.command.KCommand;

@Cmd(
        name = "kmodules",
        aliases = "km",
        permission = "kcommon.command.kmodules",
        description = "Work with modules"
)
public class KModuleCommand extends KCommand {
    @Override
    protected void onRegister() {
        addSubCommand(new KModuleReloadCommand());
        addSubCommand(new KModuleDisableCommand());
        addSubCommand(new KModuleEnableCommand());
    }
}
