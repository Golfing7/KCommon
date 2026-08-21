package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.module.test.util.FakeServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ArmorTierTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Test
    @DisplayName("Resolves the tier of a helmet, chestplate, leggings, and boots")
    void testResolvesArmorPieces() {
        assertEquals(ArmorTier.DIAMOND, ArmorTier.of(XMaterial.DIAMOND_HELMET));
        assertEquals(ArmorTier.IRON, ArmorTier.of(XMaterial.IRON_CHESTPLATE));
        assertEquals(ArmorTier.LEATHER, ArmorTier.of(XMaterial.LEATHER_BOOTS));
        assertEquals(ArmorTier.NETHERITE, ArmorTier.of(XMaterial.NETHERITE_HELMET));
    }

    @Test
    @DisplayName("Returns null for materials that are not armor")
    void testNonArmorReturnsNull() {
        assertNull(ArmorTier.of(XMaterial.DIAMOND_SWORD));
        assertNull(ArmorTier.of(XMaterial.STONE));
    }

    @Test
    @DisplayName("Resolves golden armor (XMaterial's GOLDEN_* name) to ArmorTier.GOLD")
    void testGoldenArmorResolvesToGold() {
        assertEquals(ArmorTier.GOLD, ArmorTier.of(XMaterial.GOLDEN_LEGGINGS));
    }
}
