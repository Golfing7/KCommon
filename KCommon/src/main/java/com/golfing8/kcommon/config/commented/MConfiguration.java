package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.module.Module;
import lombok.Getter;

import java.nio.file.Path;

/**
 * A {@link Configuration} with a hard link to a module.
 */
public class MConfiguration extends Configuration {
    /** The owning module of this config */
    @Getter
    private final Module module;
    public MConfiguration(Path path, Module module) {
        super(path);
        this.module = module;
    }
}
