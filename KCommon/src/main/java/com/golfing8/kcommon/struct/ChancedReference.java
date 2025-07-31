package com.golfing8.kcommon.struct;

import com.golfing8.kcommon.struct.random.RandomTestable;
import lombok.Getter;

import java.util.Random;

public final class ChancedReference<T> implements RandomTestable {
    private static final Random CHANCED_RANDOM = new Random();

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

    public boolean chance() {
        return CHANCED_RANDOM.nextDouble() * 100 < chance;
    }

    public boolean chance(double boost) {
        return CHANCED_RANDOM.nextDouble() * 100 < chance + boost;
    }
}
