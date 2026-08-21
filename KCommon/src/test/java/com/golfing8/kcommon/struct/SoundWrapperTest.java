package com.golfing8.kcommon.struct;

import com.cryptomorin.xseries.XSound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Only covers the plain data/copy-constructor behaviour of SoundWrapper.
 * send(Player)/send(Location) require a live Bukkit Player/World and
 * KCommon.getInstance() (for the delayed branch), which aren't available
 * in this test environment, so they're skipped.
 */
class SoundWrapperTest {

    @Test
    void testFieldsAndDefaultDelay() {
        SoundWrapper wrapper = new SoundWrapper(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.5f);

        assertEquals(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, wrapper.getSound());
        assertEquals(1.0f, wrapper.getVolume());
        assertEquals(1.5f, wrapper.getPitch());
        assertEquals(0, wrapper.getDelay());
    }

    @Test
    void testCopyConstructorCopiesAllFields() {
        SoundWrapper original = new SoundWrapper(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 2.0f, 5);
        SoundWrapper copy = new SoundWrapper(original);

        assertNotSame(original, copy);
        assertEquals(original.getSound(), copy.getSound());
        assertEquals(original.getVolume(), copy.getVolume());
        assertEquals(original.getPitch(), copy.getPitch());
        assertEquals(original.getDelay(), copy.getDelay());
        assertEquals(original, copy);
    }

    @Test
    void testDelayIsMutable() {
        SoundWrapper wrapper = new SoundWrapper(XSound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        wrapper.setDelay(10);
        assertEquals(10, wrapper.getDelay());
    }
}
