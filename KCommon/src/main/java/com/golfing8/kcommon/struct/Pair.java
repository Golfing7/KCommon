package com.golfing8.kcommon.struct;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * A pair of objects.
 *
 * @param <A> the left type.
 * @param <B> the right type.
 */
@Getter @Setter @AllArgsConstructor
public class Pair<A, B> {
    private A a;
    private B b;
}
