package com.golfing8.kcommon.struct;

import lombok.Data;

@Data
public class ResettableNumber {

    private final double heldValue;
    private double currentValue;

    public ResettableNumber(double value) {
        heldValue = value;
        currentValue = value;
    }

    public void reset() {
        currentValue = heldValue;
    }
}
