package com.golfing8.kcommon.module.test.config;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.config.commented.Configuration;
import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.struct.drop.DropTable;
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

    @CASerializable.Options(canDelegate = true)
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

        // Load it back.
        SimpleSerializableConf loadedItem = (SimpleSerializableConf) configuration.getWithType("conf-item", SimpleSerializableConf.class);
        assertEquals(config.confItem, loadedItem);
    }

    @Test
    public void testDelegatedValue() {
        final String delegatedConfig =
                "delegated-value:\n" +
                        "  thing1: 1\n" +
                        "  thing2: 12\n" +
                        "\n" +
                        "item1: 1\n" +
                        "item2: 1\n" +
                        "conf-item: delegated-value\n" +
                        "item-4: 11";

        Path path = Paths.get(getClass().getSimpleName() + "_testDelegatedValue.yml");
        Configuration configuration = new Configuration(path);
        configuration.loadFromString(delegatedConfig);

        SimpleSerializableConf config = (SimpleSerializableConf) configuration.getWithType("conf-item", SimpleSerializableConf.class);
        assertEquals(1, config.thing1);
        assertEquals(12, config.thing2);
    }

    @Test
    public void testDropTableSerialization() throws IOException {
        Path path = Paths.get("drop-tables.yml");
        Configuration configuration = new Configuration(path);
        if (path.toFile().exists()) {
            configuration.load();
        }
        ConfigTypeRegistry.getFromType(new ConfigEntry(configuration, "drop-tables"), DropTable.class);
    }
}
