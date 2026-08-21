package com.golfing8.kcommon.struct.item;

import com.golfing8.kcommon.module.test.util.FakeServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ItemPatchTest {

    @BeforeEach
    void setUp() {
        FakeServer.getServer();
    }

    @Nested
    @DisplayName("Patch.applyTo")
    class PatchApply {
        @Test
        @DisplayName("Replaces regex matches with the replacement")
        void testReplaces() {
            ItemPatch.Patch patch = new ItemPatch.Patch("World", "Golfing8");
            assertEquals("Hello Golfing8!", patch.applyTo("Hello World!"));
        }

        @Test
        @DisplayName("A null pattern replaces the entire string")
        void testNullPatternReplacesAll() {
            ItemPatch.Patch patch = new ItemPatch.Patch(null, "Replaced");
            assertEquals("Replaced", patch.applyTo("Anything at all"));
        }

        @Test
        @DisplayName("A null replacement is treated as an empty string")
        void testNullReplacementIsEmpty() {
            ItemPatch.Patch patch = new ItemPatch.Patch("Wor", null);
            assertEquals("ld", patch.applyTo("World"));
        }

        @Test
        @DisplayName("Compiled pattern is cached across calls")
        void testCachesCompiledPattern() {
            ItemPatch.Patch patch = new ItemPatch.Patch("a", "b");
            assertEquals(patch.applyTo("aaa"), patch.applyTo("aaa"));
            assertEquals(patch.getCompiledPattern(), patch.getCompiledPattern());
        }
    }

    @Nested
    @DisplayName("applyToItem")
    class ApplyToItem {
        @Test
        @DisplayName("Applies the material, name, and lore patches to the builder")
        void testAppliesAllPatches() {
            ItemPatch patch = new ItemPatch(
                    new ItemPatch.Patch("DIRT", "STONE"),
                    new ItemPatch.Patch("Old Name", "New Name"),
                    new ItemPatch.Patch("Old Lore", "New Lore")
            );

            ItemStackBuilder builder = new ItemStackBuilder()
                    .material(com.cryptomorin.xseries.XMaterial.DIRT)
                    .name("Old Name")
                    .lore("Old Lore");

            ItemStackBuilder patched = patch.applyToItem(builder);

            assertEquals(com.cryptomorin.xseries.XMaterial.STONE, patched.getItemType());
            assertEquals("New Name", patched.getItemName());
            assertEquals(Arrays.asList("New Lore"), patched.getItemLore());
        }

        @Test
        @DisplayName("Leaves untouched fields alone when their patch is null")
        void testPartialPatch() {
            ItemPatch patch = new ItemPatch(null, new ItemPatch.Patch("Old", "New"), null);

            ItemStackBuilder builder = new ItemStackBuilder()
                    .material(com.cryptomorin.xseries.XMaterial.DIRT)
                    .name("Old Name")
                    .lore("Original Lore");

            ItemStackBuilder patched = patch.applyToItem(builder);

            assertEquals(com.cryptomorin.xseries.XMaterial.DIRT, patched.getItemType());
            assertEquals("New Name", patched.getItemName());
            assertEquals(Arrays.asList("Original Lore"), patched.getItemLore());
        }
    }
}
