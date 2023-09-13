package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A config adapter for maps.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CAMap implements ConfigAdapter<Map> {
    @Override
    public Class<Map> getAdaptType() {
        return Map.class;
    }

    @Override
    public Map toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Collections.emptyMap();

        // The keys are assumed to be strings.
        Map values = new LinkedHashMap();
        Class<?> keyType = type.getGenericTypes().get(0);
        Class<?> valueType = type.getGenericTypes().get(1);
        FieldType keyFieldType = new FieldType(keyType);
        FieldType valueFieldType = new FieldType(valueType);
        ConfigAdapter<?> adapter = ConfigTypeRegistry.findAdapter(valueType);
        ConfigAdapter<?> keyAdapter = ConfigTypeRegistry.findAdapter(keyType);

        Map<String, Object> primitive = (Map<String, Object>) entry.getPrimitive();
        for (Map.Entry<String, Object> mapEntry : primitive.entrySet()) {
            Object adaptedKey = keyAdapter != null ? keyAdapter.toPOJO(ConfigPrimitive.ofTrusted(mapEntry.getKey()), keyFieldType) : mapEntry.getKey();

            if (adapter == null) {
                values.put(adaptedKey, mapEntry.getValue());
            } else if (mapEntry.getValue() instanceof ConfigPrimitive) {
                values.put(adaptedKey, adapter.toPOJO((ConfigPrimitive) mapEntry.getValue(), valueFieldType));
            } else {
                values.put(adaptedKey, adapter.toPOJO(ConfigPrimitive.ofTrusted(mapEntry.getValue()), valueFieldType));
            }
        }

        return values;
    }

    @Override
    public ConfigPrimitive toPrimitive(Map object) {
        Map<String, Object> primitive = new HashMap<>();
        for (Object oentry : object.entrySet()) {
            Map.Entry entry = (Map.Entry) oentry;
            ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(entry.getValue().getClass());
            if (adapter == null) {
                primitive.put(entry.getKey().toString(), entry.getValue());
            } else {
                primitive.put(entry.getKey().toString(), adapter.toPrimitive(entry.getValue()).getPrimitive());
            }
        }
        return ConfigPrimitive.ofMap(primitive);
    }
}
