package com.golfing8.kcommon.struct.random;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RandomCollectionTest {

    @Test
    void testEmptyCollectionReturnsNull() {
        RandomCollection<String> collection = new RandomCollection<>();
        assertNull(collection.next());
        assertEquals(0, collection.size());
    }

    @Test
    void testZeroOrNegativeWeightIsIgnored() {
        RandomCollection<String> collection = new RandomCollection<>();
        collection.add(0.0D, "zero");
        collection.add(-1.0D, "negative");

        assertEquals(0, collection.size());
        assertNull(collection.next());
    }

    @Test
    void testSingleEntryAlwaysReturned() {
        RandomCollection<String> collection = new RandomCollection<>();
        collection.add(10.0D, "only");

        for (int i = 0; i < 20; i++) {
            assertEquals("only", collection.next());
        }
    }

    @Test
    void testGetListReturnsAllValuesInInsertionOrder() {
        RandomCollection<String> collection = new RandomCollection<>();
        collection.add(1.0D, "a");
        collection.add(1.0D, "b");
        collection.add(1.0D, "c");

        assertEquals(Arrays.asList("a", "b", "c"), collection.getList());
        assertEquals(3, collection.size());
    }

    @Test
    void testClearEmptiesCollection() {
        RandomCollection<String> collection = new RandomCollection<>();
        collection.add(1.0D, "a");
        collection.clear();

        assertEquals(0, collection.size());
        assertNull(collection.next());
    }

    @Test
    void testGetIteratorVisitsAllValues() {
        RandomCollection<String> collection = new RandomCollection<>();
        collection.add(1.0D, "a");
        collection.add(1.0D, "b");

        Iterator<String> iterator = collection.getIterator();
        assertTrue(iterator.hasNext());
        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertFalse(iterator.hasNext());
    }
}
