package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAItemStackBuilderTest {

    @Test
    @DisplayName("Round trips basic item builder fields through a map primitive")
    void testRoundTripBasicFields() {
        ItemStackBuilder builder = new ItemStackBuilder()
                .itemType("DIAMOND_SWORD")
                .amount(3)
                .unbreakable(true)
                .name("&cSuper Sword")
                .lore("&7Line 1", "&7Line 2")
                .customModelData(42);

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(builder);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) primitive.getPrimitive();
        assertEquals("DIAMOND_SWORD", map.get("type"));
        assertEquals(3, map.get("amount"));
        assertEquals(true, map.get("unbreakable"));

        ItemStackBuilder loaded = ConfigTypeRegistry.getFromType(primitive, ItemStackBuilder.class);
        assertEquals("DIAMOND_SWORD", loaded.getItemTypeString());
        assertEquals(3, loaded.getAmount());
        assertTrue(loaded.isUnbreakable());
        assertEquals("&cSuper Sword", loaded.getItemName());
        assertEquals(Arrays.asList("&7Line 1", "&7Line 2"), loaded.getItemLore());
        assertEquals(42, loaded.getCustomModelData());
    }

    @Test
    @DisplayName("Round-trips an item with non-zero durability")
    void testDurabilityRoundTrip() {
        ItemStackBuilder builder = new ItemStackBuilder().itemType("DIAMOND_SWORD").durability((short) 5);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(builder);
        ItemStackBuilder result = ConfigTypeRegistry.getFromType(primitive, ItemStackBuilder.class);
        assertEquals((short) 5, result.getItemDurability());
    }

    @Test
    @DisplayName("Omits default/unset optional fields from the serialized map")
    void testOmitsDefaults() {
        ItemStackBuilder builder = new ItemStackBuilder().itemType("STONE");
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(builder);

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) primitive.getPrimitive();
        assertFalse(map.containsKey("unbreakable"));
        assertFalse(map.containsKey("durability"));
        assertFalse(map.containsKey("name"));
        assertFalse(map.containsKey("lore"));
    }
}
