package com.golfing8.kcommon.module.test.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class CASerializableTest {

    public static class Config extends ConfigClass {
        @Conf("Some thing as a comment")
        public int item1;
        @Conf("Some thing as a comment")
        public int item2;
        @Conf("Some thing as a comment")
        public SimpleSerializableConf confItem;
        @Conf("AAAA")
        public int item4 = 523;
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
    public void testSerializableSimple() throws IOException {
        Path path = Paths.get(getClass().getSimpleName() + "_testSerializableSimple.yml");
        Configuration configuration = new Configuration(path);
        if (path.toFile().exists()) {
            configuration.load();
        }
        Config config = new Config();
        config.confItem = new SimpleSerializableConf();

        // Create the config.
        ConfigClass configClass = new ConfigClassWrapper(null, Config.class, config);
        configClass.initConfig();
        configClass.loadValues(configuration);
        configuration.save();
//        System.out.println(configuration.saveToString());

        // Load it back.
        SimpleSerializableConf loadedItem = (SimpleSerializableConf) configuration.getWithType("conf-item", SimpleSerializableConf.class);
        assertEquals(config.confItem, loadedItem);
    }
}
