package com.golfing8.kcommon.struct;

import lombok.Getter;

import java.util.Random;

public final class ChancedReference<T> {
    private static final Random CHANCED_RANDOM = new Random();

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
