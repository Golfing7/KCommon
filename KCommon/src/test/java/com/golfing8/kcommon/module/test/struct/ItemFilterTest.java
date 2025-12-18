package com.golfing8.kcommon.module.test.struct;

import com.golfing8.kcommon.module.test.util.FakeServer;
import com.golfing8.kcommon.struct.filter.ItemFilter;
import com.golfing8.kcommon.struct.filter.StringFilter;
import com.google.common.collect.Sets;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests item filters
 */
public class ItemFilterTest {
    /**
     * Runs a simple test on item filters
     */
    @Test
    public void testSimpleFilter() {
        FakeServer.getServer();
        ItemFilter filter = new ItemFilter(
                Sets.newHashSet(new StringFilter("DIAMOND")),
                null,
                null,
                false
        );

        ItemStack stack = new ItemStack(Material.DIAMOND);
        ItemStack falseCase = new ItemStack(Material.DIAMOND_SWORD);
        assertTrue(filter.filter(stack) != 0);
        assertEquals(0, filter.filter(falseCase));
    }
}
