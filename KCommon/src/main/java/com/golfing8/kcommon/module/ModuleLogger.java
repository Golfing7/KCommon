package com.golfing8.kcommon.module;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    protected ModuleLogger(Module module) {
        super(module.getClass().getSimpleName(), null);
        this.setParent(module.getPlugin().getLogger());
        this.setLevel(Level.ALL);
    }
}
