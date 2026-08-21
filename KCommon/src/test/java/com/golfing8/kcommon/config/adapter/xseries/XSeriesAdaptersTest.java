package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XBiome;
import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XSeriesAdaptersTest {

    @Nested
    @DisplayName("CAXBiome")
    class CAXBiomeTests {
        @Test
        void testRoundTrip() {
            XBiome biome = XBiome.FOREST;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(biome);
            assertEquals("FOREST", primitive.getPrimitive());
            assertEquals(biome, ConfigTypeRegistry.getFromType(primitive, XBiome.class));
        }

        @Test
        void testUnrecognizedReturnsNull() {
            assertNull(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString("NOT_A_REAL_BIOME_XYZ"), XBiome.class));
        }
    }

    @Nested
    @DisplayName("CAXEnchantment")
    class CAXEnchantmentTests {
        @Test
        void testRoundTrip() {
            XEnchantment enchantment = XEnchantment.SHARPNESS;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(enchantment);
            assertEquals("SHARPNESS", primitive.getPrimitive());
            assertEquals(enchantment, ConfigTypeRegistry.getFromType(primitive, XEnchantment.class));
        }
    }

    @Nested
    @DisplayName("CAXEntityType")
    class CAXEntityTypeTests {
        @Test
        void testRoundTrip() {
            XEntityType entityType = XEntityType.ZOMBIE;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(entityType);
            assertEquals("ZOMBIE", primitive.getPrimitive());
            assertEquals(entityType, ConfigTypeRegistry.getFromType(primitive, XEntityType.class));
        }
    }

    @Nested
    @DisplayName("CAXMaterial")
    class CAXMaterialTests {
        @Test
        void testRoundTrip() {
            XMaterial material = XMaterial.DIAMOND_SWORD;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(material);
            assertEquals("DIAMOND_SWORD", primitive.getPrimitive());
            assertEquals(material, ConfigTypeRegistry.getFromType(primitive, XMaterial.class));
        }
    }

    @Nested
    @DisplayName("CAXPotion")
    class CAXPotionTests {
        @Test
        void testRoundTrip() {
            XPotion potion = XPotion.SPEED;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(potion);
            assertEquals("SPEED", primitive.getPrimitive());
            assertEquals(potion, ConfigTypeRegistry.getFromType(primitive, XPotion.class));
        }
    }

    @Nested
    @DisplayName("CAXSound")
    class CAXSoundTests {
        @Test
        void testRoundTrip() {
            // Unlike the other XSeries types, XSound#name() returns the underlying namespaced
            // sound key (e.g. "entity.player.levelup"), not the enum constant identifier.
            XSound sound = XSound.ENTITY_PLAYER_LEVELUP;
            ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(sound);
            assertEquals(sound.name(), primitive.getPrimitive());
            assertEquals(sound, ConfigTypeRegistry.getFromType(primitive, XSound.class));
        }
    }
}
