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

class InventoryUtilTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    private Inventory inventoryOf(ItemStack... contents) {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getContents()).thenReturn(contents);
        return inventory;
    }

    @Nested
    @DisplayName("countItems")
    class CountItems {
        @Test
        @DisplayName("Sums the amounts of items matching the predicate")
        void testCountsMatching() {
            Inventory inventory = inventoryOf(
                    new ItemStack(Material.DIAMOND, 3),
                    new ItemStack(Material.DIAMOND, 2),
                    new ItemStack(Material.STONE, 10)
            );

            int count = InventoryUtil.countItems(inventory, stack -> stack.getType() == Material.DIAMOND);
            assertEquals(5, count);
        }

        @Test
        @DisplayName("Skips null slots and air/empty stacks")
        void testSkipsNullAndAir() {
            Inventory inventory = inventoryOf(null, new ItemStack(Material.AIR), new ItemStack(Material.DIAMOND, 1));

            int count = InventoryUtil.countItems(inventory, stack -> true);
            assertEquals(1, count);
        }
    }

    @Nested
    @DisplayName("countEmptySlots")
    class CountEmptySlots {
        @Test
        @DisplayName("Counts null and air slots as empty")
        void testCountsEmpty() {
            Inventory inventory = inventoryOf(null, new ItemStack(Material.AIR), new ItemStack(Material.DIAMOND, 1));
            assertEquals(2, InventoryUtil.countEmptySlots(inventory));
        }

        @Test
        @DisplayName("Array overload matches the inventory overload")
        void testArrayOverload() {
            ItemStack[] items = new ItemStack[]{null, new ItemStack(Material.AIR), new ItemStack(Material.DIAMOND, 1)};
            assertEquals(2, InventoryUtil.countEmptySlots(items));
        }
    }

    @Nested
    @DisplayName("removeUpTo")
    class RemoveUpTo {
        @Test
        @DisplayName("Removes up to the requested amount across multiple matching stacks")
        void testRemovesAcrossStacks() {
            Inventory inventory = inventoryOf(
                    new ItemStack(Material.DIAMOND, 3),
                    new ItemStack(Material.DIAMOND, 3)
            );

            int removed = InventoryUtil.removeUpTo(inventory, 4, stack -> stack.getType() == Material.DIAMOND);
            assertEquals(4, removed);
        }

        @Test
        @DisplayName("Does not remove items that fail the predicate")
        void testIgnoresNonMatching() {
            Inventory inventory = inventoryOf(new ItemStack(Material.STONE, 5));

            int removed = InventoryUtil.removeUpTo(inventory, 5, stack -> stack.getType() == Material.DIAMOND);
            assertEquals(0, removed);
        }
    }
}
