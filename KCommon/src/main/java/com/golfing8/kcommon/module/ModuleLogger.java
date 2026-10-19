package com.golfing8.kcommon.module;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper for java loggers to allow for better logging
 */
public class ModuleLogger extends Logger {
    private final Module module;

    protected ModuleLogger(Module module) {
        super(module.getClass().getSimpleName(), null);
        this.module = module;
        this.setParent(module.getPlugin().getLogger());
        this.setLevel(Level.INFO);
    }

    @Override
    public boolean isLoggable(Level level) {
        if (module.isModuleDebug() && level == Level.FINEST) {
            return true;
        }
        return super.isLoggable(level);
    }
}
