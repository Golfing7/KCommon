package com.golfing8.kcommon.module;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class ModuleLogger extends Logger {
    private String moduleName;
    protected ModuleLogger(Module module) {
        super(module.getClass().getCanonicalName(), null);
        moduleName = module.getModuleName();
        this.setParent(module.getPlugin().getLogger());
        this.setLevel(Level.ALL);
    }

    @Override
    public void log(LogRecord record) {
        record.setMessage(this.moduleName + record.getMessage());
        super.log(record);
    }
}
