package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KNamespacedKeyTest {

    @Test
    void testConstructorAndAccessors() {
        KNamespacedKey key = new KNamespacedKey("myplugin", "my_key");
        assertEquals("myplugin", key.namespace());
        assertEquals("my_key", key.value());
        assertEquals("myplugin:my_key", key.toString());
        assertEquals("myplugin:my_key", key.asString());
    }

    @Test
    void testConstructorRejectsInvalidNamespaceOrKey() {
        assertThrows(IllegalArgumentException.class, () -> new KNamespacedKey("Invalid Namespace", "key"));
        assertThrows(IllegalArgumentException.class, () -> new KNamespacedKey("namespace", "Invalid Key!"));
        assertThrows(IllegalArgumentException.class, () -> new KNamespacedKey((String) null, "key"));
        assertThrows(IllegalArgumentException.class, () -> new KNamespacedKey("namespace", null));
    }

    @Test
    void testEqualsAndHashCode() {
        KNamespacedKey a = new KNamespacedKey("ns", "key");
        KNamespacedKey b = new KNamespacedKey("ns", "key");
        KNamespacedKey c = new KNamespacedKey("ns", "other");

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
        assertNotEquals(a, null);
        assertNotEquals(a, "ns:key");
    }

    @Test
    void testMinecraftFactory() {
        KNamespacedKey key = KNamespacedKey.minecraft("diamond");
        assertEquals(KNamespacedKey.MINECRAFT, key.namespace());
        assertEquals("diamond", key.value());
    }

    @Test
    void testFromStringWithDefaultMinecraftNamespace() {
        assertEquals(new KNamespacedKey("minecraft", "foo"), KNamespacedKey.fromString("foo"));
        assertEquals(new KNamespacedKey("foo", "bar"), KNamespacedKey.fromString("foo:bar"));
        assertEquals(new KNamespacedKey("minecraft", "foo"), KNamespacedKey.fromString(":foo", null));
    }

    @Test
    void testFromStringRejectsInvalidInput() {
        assertNull(KNamespacedKey.fromString("Foo"));
        assertNull(KNamespacedKey.fromString(""));
        assertNull(KNamespacedKey.fromString("foo:bar:baz"));
        assertThrows(IllegalArgumentException.class, () -> KNamespacedKey.fromString(null));
    }

    @Test
    void testRandomKeyUsesKcommonNamespace() {
        KNamespacedKey key = KNamespacedKey.randomKey();
        assertEquals(KNamespacedKey.KCOMMON, key.namespace());
        assertFalse(key.value().isEmpty());
    }
}
