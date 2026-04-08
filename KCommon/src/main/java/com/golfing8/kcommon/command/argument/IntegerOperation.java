package com.golfing8.kcommon.command.argument;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

/**
 * A type of operation that is applied to an integer
 * Taken from the Hytale command library.
 */
public enum IntegerOperation {
    ADD(Integer::sum, "+"),
    SUBTRACT((previous, modifier) -> previous - modifier, "-"),
    MULTIPLY((previous, modifier) -> previous * modifier, "*"),
    DIVIDE((previous, modifier) -> previous / modifier, "/"),
    MODULUS((previous, modifier) -> previous % modifier, "%"),
    SET((previous, modifier) -> modifier, "=");

    @NotNull
    private final BiFunction<Integer, Integer, Integer> operationFunction;
    @NotNull @Getter
    private final String stringRepresentation;

    IntegerOperation(@NotNull final BiFunction<Integer, Integer, Integer> operationFunction, @NotNull final String stringRepresentation) {
        this.operationFunction = operationFunction;
        this.stringRepresentation = stringRepresentation;
    }

    /**
     * Operates on the given previous number with the given modifier
     *
     * @param previous the previous
     * @param modifier the modifier
     * @return the resulting number
     */
    public int operate(int previous, int modifier) {
        return this.operationFunction.apply(previous, modifier);
    }

    /**
     * Gets the integer operation from the given string representation
     *
     * @param stringRepresentation the string representation
     * @return the operation
     */
    @Nullable
    public static IntegerOperation getFromStringRepresentation(@NotNull String stringRepresentation) {
        for (IntegerOperation value : values()) {
            if (value.stringRepresentation.equals(stringRepresentation)) {
                return value;
            }
        }

        return null;
    }
}
