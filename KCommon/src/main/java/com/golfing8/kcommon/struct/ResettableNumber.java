package com.golfing8.kcommon.struct;

import lombok.Data;

/**
 * Represents a number that can be set and reset
 */
@Data
public class ResettableNumber {

    private final double heldValue;
    private double currentValue;

    public ResettableNumber(double value) {
        heldValue = value;
        currentValue = value;
    }

    /**
     * Resets the number back to its held value
     */
    public void reset() {
        currentValue = heldValue;
    }
}
