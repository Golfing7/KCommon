package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CAEntityAttributeModifierTest {

    @Test
    @DisplayName("Round trips a fully specified modifier")
    void testRoundTripFull() {
        EntityAttributeModifier modifier = new EntityAttributeModifier(
                UUID.randomUUID(), "Custom Name", 5.0, EntityAttributeModifier.Operation.MULTIPLY_SCALAR_1, EquipmentSlot.HAND);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(modifier);

        EntityAttributeModifier loaded = ConfigTypeRegistry.getFromType(primitive, EntityAttributeModifier.class);
        assertEquals("Custom Name", loaded.getName());
        assertEquals(5.0, loaded.getAmount());
        assertEquals(EntityAttributeModifier.Operation.MULTIPLY_SCALAR_1, loaded.getOperation());
        assertEquals(EquipmentSlot.HAND, loaded.getSlot());
    }

    @Test
    @DisplayName("Defaults name, operation, and slot when absent")
    void testDefaults() {
        CAEntityAttributeModifier adapter = new CAEntityAttributeModifier();
        EntityAttributeModifier loaded = adapter.toPOJO(ConfigPrimitive.ofMap(
                java.util.Collections.singletonMap("amount", 2.5)), null);

        assertEquals("KCommon Attribute Modifier", loaded.getName());
        assertEquals(EntityAttributeModifier.Operation.ADD_NUMBER, loaded.getOperation());
        assertNull(loaded.getSlot());
        assertEquals(2.5, loaded.getAmount());
    }
}
