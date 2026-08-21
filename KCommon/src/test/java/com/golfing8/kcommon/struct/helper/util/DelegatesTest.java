package com.golfing8.kcommon.struct.helper.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class DelegatesTest {

    @Test
    void testRunnableToConsumerRunsRunnable() {
        List<String> calls = new ArrayList<>();
        Consumer<String> consumer = Delegates.runnableToConsumer(() -> calls.add("ran"));

        consumer.accept("ignored");

        assertEquals(1, calls.size());
    }

    @Test
    void testRunnableToSupplierRunsAndReturnsNull() {
        List<String> calls = new ArrayList<>();
        Supplier<Void> supplier = Delegates.runnableToSupplier(() -> calls.add("ran"));

        assertNull(supplier.get());
        assertEquals(1, calls.size());
    }

    @Test
    void testCallableToSupplierReturnsCallableResult() {
        Supplier<String> supplier = Delegates.callableToSupplier(() -> "value");
        assertEquals("value", supplier.get());
    }

    @Test
    void testCallableToSupplierWrapsCheckedException() {
        Supplier<String> supplier = Delegates.callableToSupplier(() -> {
            throw new Exception("boom");
        });

        RuntimeException thrown = assertThrows(RuntimeException.class, supplier::get);
        assertEquals("boom", thrown.getCause().getMessage());
    }

    @Test
    void testConsumerToBiConsumerFirstUsesFirstArgument() {
        List<Object> seen = new ArrayList<>();
        BiConsumer<String, Integer> biConsumer = Delegates.consumerToBiConsumerFirst(seen::add);

        biConsumer.accept("first", 2);

        assertEquals("first", seen.get(0));
    }

    @Test
    void testConsumerToBiConsumerSecondUsesSecondArgument() {
        List<Object> seen = new ArrayList<>();
        BiConsumer<String, Integer> biConsumer = Delegates.consumerToBiConsumerSecond(seen::add);

        biConsumer.accept("first", 2);

        assertEquals(2, seen.get(0));
    }

    @Test
    void testPredicateToBiPredicateFirstTestsFirstArgument() {
        BiPredicate<String, Integer> biPredicate = Delegates.predicateToBiPredicateFirst(s -> s.equals("match"));

        assertTrue(biPredicate.test("match", 0));
        assertFalse(biPredicate.test("nomatch", 0));
    }

    @Test
    void testPredicateToBiPredicateSecondTestsSecondArgument() {
        BiPredicate<String, Integer> biPredicate = Delegates.predicateToBiPredicateSecond(i -> i == 5);

        assertTrue(biPredicate.test("ignored", 5));
        assertFalse(biPredicate.test("ignored", 1));
    }

    @Test
    void testConsumerToFunctionRunsConsumerAndReturnsNull() {
        List<String> seen = new ArrayList<>();
        Function<String, Integer> function = Delegates.consumerToFunction(seen::add);

        assertNull(function.apply("value"));
        assertEquals("value", seen.get(0));
    }

    @Test
    void testRunnableToFunctionRunsRunnableAndReturnsNull() {
        List<String> calls = new ArrayList<>();
        Function<String, Integer> function = Delegates.runnableToFunction(() -> calls.add("ran"));

        assertNull(function.apply("ignored"));
        assertEquals(1, calls.size());
    }
}
