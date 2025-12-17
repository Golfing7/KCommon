package com.golfing8.kcommon.struct.placeholder;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;

/**
 * An abstract type of placeholder that parses on an object and produces output
 *
 * @param <I> the input type
 * @param <O> the output type
 */
public abstract class PlaceholderAbstract<I, O> {
    /**
     * If this placeholder is trusted, the contents of the placeholders will also be parsed.
     */
    @Getter
    @Setter
    private boolean trusted;

    public PlaceholderAbstract() {
    }

    public PlaceholderAbstract(boolean trusted) {
        this.trusted = trusted;
    }

    /**
     * Applies this placeholder to the given types.
     *
     * @return the applied placeholder.
     */
    @Contract(pure = true)
    public abstract O apply(I in);
}
