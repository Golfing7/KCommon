package com.golfing8.kcommon.struct.currency;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyTest {

    @Test
    @DisplayName("Default MONEY formatting applies the dollar-sign format")
    void testDefaultMoneyFormat() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0);
        assertEquals("$5", currency.format(1));
    }

    @Test
    @DisplayName("Default EXP formatting applies the Exp suffix")
    void testDefaultExpFormat() {
        Currency currency = new Currency(EconomyType.EXP, 10.0);
        assertEquals("10 Exp", currency.format(1));
    }

    @Test
    @DisplayName("A format override replaces the default formatting")
    void testFormatOverride() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0, "{AMOUNT} coins");
        assertEquals("5 coins", currency.format(1));
    }

    @Test
    @DisplayName("format(amount) multiplies the base amount by the given multiplier")
    void testFormatMultipliesAmount() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0);
        assertEquals("$15", currency.format(3));
    }

    @Test
    @DisplayName("A custom format string is used verbatim")
    void testCustomFormat() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0);
        assertEquals("You have 5!", currency.format(1, "You have {AMOUNT}!"));
    }

    @Test
    @DisplayName("add() returns a new currency with the summed amount")
    void testAdd() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0);
        Currency added = currency.add(2.5);
        assertEquals(7.5, added.getAmount());
        // Original is untouched
        assertEquals(5.0, currency.getAmount());
    }

    @Test
    @DisplayName("times() returns a new currency with the multiplied amount")
    void testTimes() {
        Currency currency = new Currency(EconomyType.MONEY, 5.0);
        Currency multiplied = currency.times(3.0);
        assertEquals(15.0, multiplied.getAmount());
        assertEquals(5.0, currency.getAmount());
    }

    @Test
    @DisplayName("Constructing with a negative amount throws")
    void testNegativeAmountThrows() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> new Currency(EconomyType.MONEY, -1.0));
    }
}
