package com.golfing8.kcommon.command.impl;

import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.KCommand;

/**
 * Parent command for all KModule management commands
 */
@Cmd(
        name = "kmodules",
        aliases = "km",
        description = "Work with modules"
)
public class KModuleCommand extends KCommand {
    @Override
    protected void onRegister() {
        addSubCommand(new KModuleReloadCommand());
        addSubCommand(new KModuleDisableCommand());
        addSubCommand(new KModuleEnableCommand());
        addSubCommand(new KModuleListCommand());
        addSubCommand(new KModulePlaceholders());
    }
}
