package com.golfing8.kcommon.struct;

/**
 * A pair of objects.
 *
 * @param <A> the left type.
 * @param <B> the right type.
 */
public class Pair<A, B> {
    private A a;
    private B b;

    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() {
        return a;
    }

    public B getB() {
        return b;
    }

    public void setA(A a) {
        this.a = a;
    }

    public void setB(B b) {
        this.b = b;
    }
}
