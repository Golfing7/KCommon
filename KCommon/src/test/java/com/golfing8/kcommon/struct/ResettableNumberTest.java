package com.golfing8.kcommon.struct;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResettableNumberTest {

    @Test
    void testConstructorSetsHeldAndCurrentValue() {
        ResettableNumber number = new ResettableNumber(5.0D);
        assertEquals(5.0D, number.getHeldValue());
        assertEquals(5.0D, number.getCurrentValue());
    }

    @Test
    void testResetRestoresHeldValueAfterMutation() {
        ResettableNumber number = new ResettableNumber(5.0D);
        number.setCurrentValue(100.0D);
        assertEquals(100.0D, number.getCurrentValue());

        number.reset();
        assertEquals(5.0D, number.getCurrentValue());
        assertEquals(5.0D, number.getHeldValue());
    }
}
