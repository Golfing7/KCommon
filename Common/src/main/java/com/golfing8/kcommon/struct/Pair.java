package com.golfing8.kcommon.struct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * A pair of objects.
 *
 * @param <A> the left type.
 * @param <B> the right type.
 */
@Getter @Setter @AllArgsConstructor
public class Pair<A, B> implements Map.Entry<A, B> {
    private A a;
    private B b;

    @Override
    public A getKey() {
        return a;
    }

    @Override
    public B getValue() {
        return b;
    }

    @Override
    public B setValue(B value) {
        B old = b;
        b = value;
        return old;
    }
}
