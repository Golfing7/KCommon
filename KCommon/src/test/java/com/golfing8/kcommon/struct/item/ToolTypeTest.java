package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.module.test.util.FakeServer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolTypeTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Test
    @DisplayName("Matches swords, axes, pickaxes, shovels, and hoes by material name suffix")
    void testMatchesByMaterialSuffix() {
        assertTrue(ToolType.SWORD.isSameType(XMaterial.DIAMOND_SWORD));
        assertTrue(ToolType.AXE.isSameType(XMaterial.IRON_AXE));
        assertTrue(ToolType.PICKAXE.isSameType(XMaterial.GOLDEN_PICKAXE));
        assertTrue(ToolType.SHOVEL.isSameType(XMaterial.WOODEN_SHOVEL));
        assertTrue(ToolType.HOE.isSameType(XMaterial.STONE_HOE));
    }

    @Test
    @DisplayName("Does not cross-match different tool types")
    void testDoesNotCrossMatch() {
        assertFalse(ToolType.SWORD.isSameType(XMaterial.IRON_AXE));
        assertFalse(ToolType.AXE.isSameType(XMaterial.DIAMOND_SWORD));
    }

    @Test
    @DisplayName("Bow and fishing rod match a single exact material")
    void testExactMaterialMatches() {
        assertTrue(ToolType.BOW.isSameType(XMaterial.BOW));
        assertTrue(ToolType.BOW.isSameType(XMaterial.CROSSBOW));
        assertTrue(ToolType.FISHING_ROD.isSameType(XMaterial.FISHING_ROD));
        assertFalse(ToolType.FISHING_ROD.isSameType(XMaterial.BOW));
    }

    @Test
    @DisplayName("isSameType accepts a real ItemStack")
    void testMatchesItemStack() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        assertTrue(ToolType.SWORD.isSameType(sword));

        ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
        assertFalse(ToolType.SWORD.isSameType(pickaxe));
    }
}
