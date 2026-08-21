package com.golfing8.kcommon.config.adapter;

import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.exc.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.struct.SoundWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CASoundWrapperTest {

    @Test
    @DisplayName("Round trips a sound wrapper through a map primitive")
    void testRoundTrip() {
        SoundWrapper wrapper = new SoundWrapper(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5F, 1.5F, 5);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(wrapper);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) primitive.getPrimitive();
        assertEquals(XSound.ENTITY_EXPERIENCE_ORB_PICKUP.name(), map.get("sound"));

        SoundWrapper loaded = ConfigTypeRegistry.getFromType(primitive, SoundWrapper.class);
        assertEquals(wrapper.getSound(), loaded.getSound());
        assertEquals(wrapper.getVolume(), loaded.getVolume());
        assertEquals(wrapper.getPitch(), loaded.getPitch());
        assertEquals(wrapper.getDelay(), loaded.getDelay());
    }

    @Test
    @DisplayName("Defaults pitch/volume to 1.0 and delay to 0 when absent")
    void testDefaults() {
        CASoundWrapper adapter = new CASoundWrapper();
        SoundWrapper loaded = adapter.toPOJO(ConfigPrimitive.ofMap(
                java.util.Collections.singletonMap("sound", "ENTITY_EXPERIENCE_ORB_PICKUP")), null);
        assertEquals(1.0F, loaded.getPitch());
        assertEquals(1.0F, loaded.getVolume());
        assertEquals(0, loaded.getDelay());
    }

    @Test
    @DisplayName("Throws for an unrecognized sound name")
    void testUnrecognizedSoundThrows() {
        CASoundWrapper adapter = new CASoundWrapper();
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(
                java.util.Collections.singletonMap("sound", "NOT_A_REAL_SOUND_NAME_AT_ALL"));
        assertThrows(ImproperlyConfiguredValueException.class, () -> adapter.toPOJO(primitive, null));
    }
}
