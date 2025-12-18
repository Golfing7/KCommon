package com.golfing8.kcommon.module.test;

import com.golfing8.kcommon.config.generator.ConfigClassSource;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;

class TestSource implements ConfigClassSource {

}

/**
 * A test implementation of a {@link Module}
 */
@ModuleInfo(
        name = "test",
        configSources = TestSource.class
)
public class TestModule extends Module {
    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
