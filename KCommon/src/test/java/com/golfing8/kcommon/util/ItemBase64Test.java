package com.golfing8.kcommon.util;

import com.golfing8.kcommon.module.test.util.FakeServer;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ItemBase64Test {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Nested
    @DisplayName("Single item round trip")
    class SingleItem {
        @Test
        @DisplayName("Encoding and then decoding an item preserves its type and amount")
        void testRoundTrip() throws Exception {
            ItemStack original = new ItemStack(Material.DIAMOND_SWORD, 3);
            String encoded = ItemBase64.toBase64(original);
            ItemStack decoded = ItemBase64.itemStackFromBase64(encoded);

            assertEquals(original.getType(), decoded.getType());
            assertEquals(original.getAmount(), decoded.getAmount());
        }
    }

    @Nested
    @DisplayName("Item array round trip")
    class ItemArray {
        @Test
        @DisplayName("Encoding and then decoding an array preserves every item, including nulls")
        void testRoundTrip() throws Exception {
            ItemStack[] original = new ItemStack[]{
                    new ItemStack(Material.DIAMOND, 5),
                    null,
                    new ItemStack(Material.STONE, 64)
            };
            String encoded = ItemBase64.itemStackArrayToBase64(original);
            ItemStack[] decoded = ItemBase64.itemStackArrayFromBase64(encoded);

            assertEquals(3, decoded.length);
            assertEquals(Material.DIAMOND, decoded[0].getType());
            assertEquals(5, decoded[0].getAmount());
            assertEquals(null, decoded[1]);
            assertEquals(Material.STONE, decoded[2].getType());
        }
    }

    @Nested
    @DisplayName("Inventory encoding")
    class InventoryEncoding {
        @Test
        @DisplayName("toBase64(Inventory) serializes every slot, decodable as an item array")
        void testEncodesInventoryContents() throws Exception {
            Inventory inventory = mock(Inventory.class);
            when(inventory.getSize()).thenReturn(2);
            when(inventory.getItem(0)).thenReturn(new ItemStack(Material.GOLD_INGOT, 1));
            when(inventory.getItem(1)).thenReturn(null);

            String encoded = ItemBase64.toBase64(inventory);
            ItemStack[] decoded = ItemBase64.itemStackArrayFromBase64(encoded);

            assertEquals(2, decoded.length);
            assertEquals(Material.GOLD_INGOT, decoded[0].getType());
            assertEquals(null, decoded[1]);
        }
    }
}
