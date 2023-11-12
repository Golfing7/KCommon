package com.golfing8.kcommon.module.test.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CASerializableTest {

    public static class Config extends ConfigClass {
        public int item1;
        public int item2;
        public SimpleSerializableConf confItem;
    }

    public static class SimpleSerializableConf implements CASerializable {
        public int thing1 = 5;
        public int thing2 = 6;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SimpleSerializableConf that = (SimpleSerializableConf) o;
            return thing1 == that.thing1 && thing2 == that.thing2;
        }

        @Override
        public int hashCode() {
            return Objects.hash(thing1, thing2);
        }
    }

    @Test
    public void testSerializableSimple() {
        Path path = Paths.get(getClass().getSimpleName() + "_testSerializableSimple.yml");
        Configuration configuration = new Configuration(path);
        Config config = new Config();
        config.confItem = new SimpleSerializableConf();

        // Create the config.
        ConfigClass configClass = new ConfigClassWrapper(null, Config.class, config);
        configClass.initConfig();
        configClass.loadValues(configuration);

        // Load it back.
        SimpleSerializableConf loadedItem = (SimpleSerializableConf) configuration.getWithType("conf-item", SimpleSerializableConf.class);
        assertEquals(config.confItem, loadedItem);
    }
}
