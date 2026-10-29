package com.golfing8.kcommon.struct;

import com.golfing8.kcommon.struct.random.RandomTestable;
import lombok.Getter;

import java.util.Optional;

/**
 * Wraps an object with methods for getting the object
 *
 * @param <T> the type
 */
public final class ChancedReference<T> implements RandomTestable {
    @Getter
    private final double chance;
    @Getter
    private final T reference;

    public ChancedReference(T reference) {
        this(100.0D, reference);
    }

    public ChancedReference(double chance, T reference) {
        this.chance = chance;
        this.reference = reference;
    }

    /**
     * Gets the object with the odds in this instance
     *
     * @return the optional of the object
     */
    public Optional<T> get() {
        return get(1);
    }

    /**
     * Gets the object with the given boost
     *
     * @param boost the boost
     * @return an optional of the object
     */
    public Optional<T> get(double boost) {
        if (testRandom(boost))
            return Optional.of(reference);
        else
            return Optional.empty();
    }
}
