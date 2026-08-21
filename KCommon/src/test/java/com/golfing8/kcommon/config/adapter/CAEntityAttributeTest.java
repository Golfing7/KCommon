package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAEntityAttributeTest {

    @Test
    @DisplayName("Round trips an entity attribute by its enum name")
    void testRoundTrip() {
        EntityAttribute attribute = EntityAttribute.GENERIC_MAX_HEALTH;
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(attribute);
        assertEquals("GENERIC_MAX_HEALTH", primitive.getPrimitive());

        EntityAttribute loaded = ConfigTypeRegistry.getFromType(primitive, EntityAttribute.class);
        assertEquals(attribute, loaded);
    }

    @Test
    @DisplayName("Resolves an attribute by one of its legacy old names")
    void testResolvesByOldName() {
        EntityAttribute loaded = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString("MAX_HEALTH"), EntityAttribute.class);
        assertEquals(EntityAttribute.GENERIC_MAX_HEALTH, loaded);
    }
}
