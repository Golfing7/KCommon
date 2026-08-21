package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.struct.PotionData;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAPotionDataTest {

    @Test
    @DisplayName("Round trips potion type through a map primitive")
    void testRoundTripType() {
        // Constructor order is (potionType, amplified, extended).
        PotionData data = new PotionData(PotionType.STRENGTH, true, false);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(data);

        PotionData loaded = ConfigTypeRegistry.getFromType(primitive, PotionData.class);
        assertEquals(PotionType.STRENGTH, loaded.getPotionType());
    }

    @Test
    @DisplayName("Round trip preserves the amplified and extended flags")
    void testRoundTripPreservesAmplifiedAndExtended() {
        PotionData data = new PotionData(PotionType.STRENGTH, true, false);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(data);

        PotionData loaded = ConfigTypeRegistry.getFromType(primitive, PotionData.class);
        assertTrue(loaded.isAmplified());
        assertFalse(loaded.isExtended());
    }

    @Test
    @DisplayName("Defaults amplified/extended to false when absent")
    void testDefaults() {
        CAPotionData adapter = new CAPotionData();
        PotionData loaded = adapter.toPOJO(ConfigPrimitive.ofMap(
                java.util.Collections.singletonMap("potion-type", "WATER")), null);
        assertFalse(loaded.isAmplified());
        assertFalse(loaded.isExtended());
    }
}
