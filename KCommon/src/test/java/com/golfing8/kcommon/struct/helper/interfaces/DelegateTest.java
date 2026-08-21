package com.golfing8.kcommon.struct.helper.interfaces;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class DelegateTest {

    static class Wrapper<T> implements Delegate<T> {
        private final T delegate;

        Wrapper(T delegate) {
            this.delegate = delegate;
        }

        @Override
        public T getDelegate() {
            return delegate;
        }
    }

    @Test
    void testResolveUnwrapsSingleDelegate() {
        Object real = new Object();
        Wrapper<Object> wrapper = new Wrapper<>(real);

        assertSame(real, Delegate.resolve(wrapper));
    }

    @Test
    void testResolveUnwrapsNestedDelegates() {
        Object real = new Object();
        Wrapper<Object> inner = new Wrapper<>(real);
        Wrapper<Object> outer = new Wrapper<>(inner);

        assertSame(real, Delegate.resolve(outer));
    }

    @Test
    void testResolveReturnsNonDelegateAsIs() {
        Object plain = "plain value";
        assertEquals("plain value", Delegate.resolve(plain));
    }
}
