package com.golfing8.kcommon.struct.ptr;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldPointerTest {

    static class Holder {
        String value = "initial";
    }

    @Test
    void testGetReadsFieldFromInstance() {
        Holder holder = new Holder();
        FieldHandle<String> handle = new FieldHandle<>("value", Holder.class);
        FieldPointer<String> pointer = new FieldPointer<>(holder, handle);

        assertEquals("initial", pointer.get());
    }

    @Test
    void testSetWritesFieldOnInstance() {
        Holder holder = new Holder();
        FieldHandle<String> handle = new FieldHandle<>("value", Holder.class);
        FieldPointer<String> pointer = new FieldPointer<>(holder, handle);

        pointer.set("updated");

        assertEquals("updated", holder.value);
        assertEquals("updated", pointer.get());
    }

    @Test
    void testEqualsComparesCurrentFieldValue() {
        Holder holderA = new Holder();
        Holder holderB = new Holder();
        FieldHandle<String> handle = new FieldHandle<>("value", Holder.class);

        FieldPointer<String> pointerA = new FieldPointer<>(holderA, handle);
        FieldPointer<String> pointerB = new FieldPointer<>(holderB, handle);

        assertEquals(pointerA, pointerB);

        pointerB.set("different");
        assertNotEquals(pointerA, pointerB);
    }
}
