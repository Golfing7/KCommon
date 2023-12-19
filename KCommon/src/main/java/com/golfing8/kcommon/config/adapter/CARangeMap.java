package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.map.RangeMap;
import com.golfing8.kcommon.struct.reflection.FieldType;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A config adapter for maps.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CARangeMap implements ConfigAdapter<RangeMap> {
    @Override
    public Class<RangeMap> getAdaptType() {
        return RangeMap.class;
    }

    @Override
    public RangeMap toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return new RangeMap();

        RangeMap rangeMap = new RangeMap();

        // The keys are assumed to be strings.
        Type keyType = Range.class;
        Type valueType = type.getGenericTypes().get(0);
        FieldType keyFieldType = new FieldType(keyType);
        FieldType valueFieldType = new FieldType(valueType);
        ConfigAdapter<?> adapter = ConfigTypeRegistry.findAdapter(valueType);
        ConfigAdapter<?> keyAdapter = ConfigTypeRegistry.findAdapter(keyType);
        if (keyAdapter == null)
            throw new IllegalStateException("Range config adapter does not exist!");

        Map<String, Object> primitive = entry.unwrap();
        for (Map.Entry<String, Object> mapEntry : primitive.entrySet()) {
            Range adaptedKey = (Range) keyAdapter.toPOJO(ConfigPrimitive.ofTrusted(ConfigPrimitive.safeKeyStringToString(mapEntry.getKey())), keyFieldType);

            if (adapter == null) {
                rangeMap.put(adaptedKey, mapEntry.getValue());
            } else {
                rangeMap.put(adaptedKey, adapter.toPOJO(ConfigPrimitive.ofTrusted(mapEntry.getValue()), valueFieldType));
            }
        }

        return rangeMap;
    }

    @Override
    public ConfigPrimitive toPrimitive(RangeMap object) {
        Map<String, Object> primitive = new HashMap<>();
        for (Object oentry : object.entrySet()) {
            Map.Entry entry = (Map.Entry) oentry;
            ConfigAdapter valueAdapter = ConfigTypeRegistry.findAdapter(entry.getValue().getClass());
            ConfigAdapter keyAdapter = ConfigTypeRegistry.findAdapter(entry.getKey().getClass());
            Object adaptedKey = keyAdapter == null ? ConfigPrimitive.coerceBoxedToString(entry.getKey()) : keyAdapter.toPrimitive(entry.getKey()).unwrap();
            Object adaptedValue = valueAdapter == null ? entry.getValue() : valueAdapter.toPrimitive(entry.getValue()).unwrap();
            if (!(adaptedKey instanceof String))
                throw new IllegalStateException(String.format("Cannot serialize map with non string-like key %s", adaptedKey));

            primitive.put((String) adaptedKey, adaptedValue);
        }
        return ConfigPrimitive.ofMap(primitive);
    }
}
