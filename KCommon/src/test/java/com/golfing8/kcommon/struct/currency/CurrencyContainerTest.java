package com.golfing8.kcommon.struct.currency;

import com.golfing8.kcommon.struct.Pair;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyContainerTest {

    @Test
    @DisplayName("Set and get round-trips a currency amount")
    void testSetAndGetCurrency() {
        CurrencyContainer container = new CurrencyContainer();
        container.setCurrency(EconomyType.MONEY, 100.0);
        assertEquals(100.0, container.getCurrency(EconomyType.MONEY));
    }

    @Test
    @DisplayName("Getting an unset currency type returns zero")
    void testUnsetCurrencyReturnsZero() {
        CurrencyContainer container = new CurrencyContainer();
        assertEquals(0.0, container.getCurrency(EconomyType.EXP));
    }

    @Test
    @DisplayName("multiply scales every stored currency amount")
    void testMultiply() {
        CurrencyContainer container = new CurrencyContainer();
        container.setCurrency(EconomyType.MONEY, 10.0);
        container.setCurrency(EconomyType.EXP, 20.0);
        container.multiply(2.0);
        assertEquals(20.0, container.getCurrency(EconomyType.MONEY));
        assertEquals(40.0, container.getCurrency(EconomyType.EXP));
    }

    @Test
    @DisplayName("add sums matching currency types and adds missing ones")
    void testAdd() {
        CurrencyContainer a = new CurrencyContainer();
        a.setCurrency(EconomyType.MONEY, 10.0);

        CurrencyContainer b = new CurrencyContainer();
        b.setCurrency(EconomyType.MONEY, 5.0);
        b.setCurrency(EconomyType.EXP, 15.0);

        a.add(b);

        assertEquals(15.0, a.getCurrency(EconomyType.MONEY));
        assertEquals(15.0, a.getCurrency(EconomyType.EXP));
    }

    @Test
    @DisplayName("clone produces an independent copy")
    void testClone() {
        CurrencyContainer original = new CurrencyContainer();
        original.setCurrency(EconomyType.MONEY, 10.0);

        CurrencyContainer cloned = original.clone();
        cloned.setCurrency(EconomyType.MONEY, 20.0);

        assertEquals(10.0, original.getCurrency(EconomyType.MONEY));
        assertEquals(20.0, cloned.getCurrency(EconomyType.MONEY));
    }

    @Test
    @DisplayName("getAllCurrencies returns a pair for every stored currency")
    void testGetAllCurrencies() {
        CurrencyContainer container = new CurrencyContainer();
        container.setCurrency(EconomyType.MONEY, 10.0);
        container.setCurrency(EconomyType.EXP, 20.0);

        List<Pair<EconomyType, Currency>> all = container.getAllCurrencies();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Iteration yields currencies sorted in descending order by default")
    void testIterationSortOrder() {
        CurrencyContainer container = new CurrencyContainer();
        container.setCurrency(EconomyType.MONEY, 5.0);
        container.setCurrency(EconomyType.EXP, 50.0);

        List<Pair<EconomyType, Currency>> ordered = new java.util.ArrayList<>();
        container.forEach(ordered::add);

        assertEquals(EconomyType.EXP, ordered.get(0).getKey());
        assertEquals(EconomyType.MONEY, ordered.get(1).getKey());
    }

    @Test
    @DisplayName("formatPlaceholder produces one formatted line per currency")
    void testFormatPlaceholder() {
        CurrencyContainer container = new CurrencyContainer();
        container.setCurrency(EconomyType.MONEY, 5.0);
        container.setCurrency(EconomyType.EXP, 10.0);

        assertEquals(2, container.formatPlaceholder().getReplacement().size());
    }
}
