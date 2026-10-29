package com.golfing8.kcommon.module;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A wrapper for java loggers to allow for better logging
 */
public class ModuleLogger extends Logger {
    private final AbstractModule<?> module;

    protected ModuleLogger(AbstractModule<?> module) {
        super(module.getClass().getSimpleName(), null);
        this.module = module;
        this.setParent(module.getPlugin().getPluginLogger());
        this.setLevel(Level.INFO);
    }

    @Override
    public boolean isLoggable(Level level) {
        if (module.isModuleDebug() && level.intValue() < Level.INFO.intValue()) {
            return true;
        }
        return super.isLoggable(level);
    }
}
