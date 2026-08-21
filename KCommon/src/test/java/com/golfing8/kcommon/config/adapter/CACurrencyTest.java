package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.currency.Currency;
import com.golfing8.kcommon.struct.currency.EconomyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CACurrencyTest {

    @Test
    @DisplayName("Round trips a currency's type and amount")
    void testRoundTripsCurrency() {
        Currency original = new Currency(EconomyType.EXP, 250.0);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Currency result = ConfigTypeRegistry.getFromType(primitive, Currency.class);
        assertEquals(EconomyType.EXP, result.getEconomyType());
        assertEquals(250.0, result.getAmount());
    }

    @Test
    @DisplayName("A bare numeric primitive is interpreted as a MONEY amount")
    void testNumericPrimitiveIsMoney() {
        ConfigPrimitive primitive = ConfigPrimitive.ofDouble(99.5);
        Currency result = ConfigTypeRegistry.getFromType(primitive, Currency.class);
        assertEquals(EconomyType.MONEY, result.getEconomyType());
        assertEquals(99.5, result.getAmount());
    }

    @Test
    @DisplayName("Missing 'type' defaults to MONEY")
    void testMissingTypeDefaultsToMoney() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("value", 10.0);
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        Currency result = ConfigTypeRegistry.getFromType(primitive, Currency.class);
        assertEquals(EconomyType.MONEY, result.getEconomyType());
        assertEquals(10.0, result.getAmount());
    }

    @Test
    @DisplayName("Missing 'value' throws an InvalidConfigException")
    void testMissingValueThrows() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "MONEY");
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        assertThrows(InvalidConfigException.class,
                () -> ConfigTypeRegistry.getFromType(primitive, Currency.class));
    }

    @Test
    @DisplayName("An unrecognized economy type throws an InvalidConfigException")
    void testUnrecognizedEconomyTypeThrows() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "NOT_REAL");
        map.put("value", 10.0);
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        assertThrows(InvalidConfigException.class,
                () -> ConfigTypeRegistry.getFromType(primitive, Currency.class));
    }
}
