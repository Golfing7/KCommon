package com.golfing8.kcommon.struct.blocks;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WeightedCollectionTest {

    @Test
    void testEmptyCollectionReturnsNull() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        assertNull(collection.get());
    }

    @Test
    void testSingleObjectAlwaysReturned() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        collection.addWeightedObject("only", 10.0D);

        for (int i = 0; i < 20; i++) {
            assertEquals("only", collection.get());
        }
    }

    @Test
    void testVarargsConstructorRegistersAllValues() {
        WeightedCollection<String> collection = new WeightedCollection<>("a", 1.0D, "b", 1.0D, "c", 1.0D);
        assertEquals(Sets.newHashSet("a", "b", "c"), collection.getAll());
    }

    @Test
    void testGetChanceMapIsUnmodifiable() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        collection.addWeightedObject("a", 1.0D);
        assertThrows(UnsupportedOperationException.class, () -> collection.getChanceMap().put("b", 2.0D));
    }

    @Test
    void testRemoveExcludesFromResults() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        collection.addWeightedObject("a", 1.0D);
        collection.addWeightedObject("b", 1.0D);
        collection.remove("a");

        assertEquals(Sets.newHashSet("b"), collection.getAll());
        for (int i = 0; i < 20; i++) {
            assertEquals("b", collection.get());
        }
    }

    @Test
    void testClearEmptiesCollection() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        collection.addWeightedObject("a", 1.0D);
        collection.clear();

        assertTrue(collection.getAll().isEmpty());
        assertNull(collection.get());
    }

    @Test
    void testRemoveAllExcludesMultipleValues() {
        WeightedCollection<String> collection = new WeightedCollection<>();
        collection.addWeightedObject("a", 1.0D);
        collection.addWeightedObject("b", 1.0D);
        collection.addWeightedObject("c", 1.0D);
        collection.removeAll(Sets.newHashSet("a", "b"));

        assertEquals(Sets.newHashSet("c"), collection.getAll());
    }
}
