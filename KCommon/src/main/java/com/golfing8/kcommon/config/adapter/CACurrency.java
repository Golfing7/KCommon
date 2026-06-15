package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.currency.Currency;
import com.golfing8.kcommon.struct.currency.EconomyType;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A config adapter for the {@link com.golfing8.kcommon.struct.currency.Currency} type
 */
public class CACurrency implements ConfigAdapter<Currency> {
    @Override
    public Class<Currency> getAdaptType() {
        return Currency.class;
    }

    @Override
    public @Nullable Currency toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null) {
            return null;
        }

        if (entry.getPrimitive() instanceof Number) {
            double value = ((Number) entry.getPrimitive()).doubleValue();
            return new Currency(EconomyType.MONEY, value);
        } else {
            EconomyType economyType;
            ConfigPrimitive typePrimitive = entry.getSubValue("type");
            if (typePrimitive.getPrimitive() == null) {
                economyType = EconomyType.MONEY;
            } else {
                try {
                    economyType = EconomyType.valueOf(typePrimitive.getPrimitive().toString());
                } catch (IllegalArgumentException exc) {
                    throw new InvalidConfigException("Economy type " + typePrimitive.getPrimitive() + " does not exist!", exc);
                }
            }
            ConfigPrimitive valuePrimitive = entry.getSubValue("value");
            if (valuePrimitive.getPrimitive() == null) {
                throw new InvalidConfigException("Value not defined for currency");
            }

            String formatOverride = null;
            ConfigPrimitive formatPrimitive = entry.getSubValue("format");
            if (formatPrimitive.getPrimitive() != null) {
                formatOverride = formatPrimitive.getPrimitive().toString();
            }
            return new Currency(economyType, ConfigPrimitive.coerceObjectToBoxed(valuePrimitive.getPrimitive(), Double.class), formatOverride);
        }
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Currency object) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", object.getEconomyType().name());
        data.put("value", object.getAmount());
        return ConfigPrimitive.ofMap(data);
    }
}
