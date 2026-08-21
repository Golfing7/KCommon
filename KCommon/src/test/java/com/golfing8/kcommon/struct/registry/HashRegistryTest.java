package com.golfing8.kcommon.struct.registry;

import net.kyori.adventure.key.Key;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class HashRegistryTest {

    private static final Key KEY_A = Key.key("kcommon", "a");
    private static final Key KEY_B = Key.key("kcommon", "b");

    @Test
    void testRegisterAndGetByKey() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");

        Optional<String> found = registry.get(KEY_A);
        assertTrue(found.isPresent());
        assertEquals("valueA", found.get());
    }

    @Test
    void testGetByNameUsesKeyValuePortion() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");

        Optional<String> found = registry.getByName("a");
        assertTrue(found.isPresent());
        assertEquals("valueA", found.get());
    }

    @Test
    void testGetMissingKeyReturnsEmpty() {
        HashRegistry<String> registry = new HashRegistry<>();
        assertFalse(registry.get(KEY_A).isPresent());
        assertFalse(registry.getByName("missing").isPresent());
    }

    @Test
    void testUnregisterRemovesFromBothLookups() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");

        String removed = registry.unregister(KEY_A);
        assertEquals("valueA", removed);
        assertFalse(registry.get(KEY_A).isPresent());
        assertFalse(registry.getByName("a").isPresent());
    }

    @Test
    void testUnregisterMissingKeyReturnsNull() {
        HashRegistry<String> registry = new HashRegistry<>();
        assertNull(registry.unregister(KEY_A));
    }

    @Test
    void testEntriesAndElementsReflectRegisteredValues() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");
        registry.register(KEY_B, "valueB");

        assertEquals(2, registry.entries().size());
        assertTrue(registry.elements().contains("valueA"));
        assertTrue(registry.elements().contains("valueB"));
    }

    @Test
    void testIteratorVisitsAllElements() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");

        Iterator<String> iterator = registry.iterator();
        assertTrue(iterator.hasNext());
        assertEquals("valueA", iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    void testEntriesCollectionIsUnmodifiable() {
        HashRegistry<String> registry = new HashRegistry<>();
        registry.register(KEY_A, "valueA");

        assertThrows(UnsupportedOperationException.class, () -> registry.entries().clear());
    }
}
