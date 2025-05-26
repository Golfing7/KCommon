package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.map.RangeMap;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

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
public class CAMap implements ConfigAdapter<Map> {
    @Override
    public Class<Map> getAdaptType() {
        return Map.class;
    }

    @Override
    public Map toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Collections.emptyMap();

        // Check if we can reflectively find the type of map that was being used.
        Map values;
        if (!type.getType().isInterface() && (type.getType().getModifiers() & Modifier.ABSTRACT) == 0) {
            try {
                values = (Map) type.getType().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(String.format("Failed to instantiate map with type %s", type.getType().getName()), e);
            }
        } else {
            values = new LinkedHashMap();
        }
        // The keys are assumed to be strings.
        Type keyType = type.getGenericTypes().get(0);
        Type valueType = type.getGenericTypes().get(1);
        FieldType keyFieldType = new FieldType(keyType);
        FieldType valueFieldType = new FieldType(valueType);
        ConfigAdapter<?> keyAdapter = ConfigTypeRegistry.findAdapter(keyType);
        ConfigAdapter<?> valueAdapter = ConfigTypeRegistry.findAdapter(valueType);

        Map<String, Object> primitive = entry.unwrap();
        for (Map.Entry<String, Object> mapEntry : primitive.entrySet()) {
            Object adaptedKey = keyAdapter != null ?
                    keyAdapter.toPOJO(ConfigPrimitive.ofTrusted(mapEntry.getKey()), keyFieldType) :
                    ConfigPrimitive.coerceObjectToBoxed(mapEntry.getKey(), keyFieldType.getType());

            if (valueAdapter == null) {
                if (ConfigPrimitive.isYamlPrimitive(valueFieldType.getType())) {
                    values.put(adaptedKey, ConfigPrimitive.coerceObjectToBoxed(mapEntry.getValue().toString(), valueFieldType.getType()));
                } else {
                    values.put(adaptedKey, mapEntry.getValue());
                }
            } else {
                try {
                    values.put(adaptedKey, valueAdapter.toPOJO(entry.getSubValue(mapEntry.getKey()), valueFieldType));
                } catch (Throwable thr) {
                    throw new RuntimeException(String.format("Failed to load map value with type %s under path %s!", valueType.getTypeName(), entry.formatPath(mapEntry.getKey())), thr);
                }
            }
        }

        return values;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Map object) {
        Map<String, Object> primitive = new HashMap<>();
        for (Object oentry : object.entrySet()) {
            Map.Entry entry = (Map.Entry) oentry;
            ConfigAdapter valueAdapter = entry.getValue() != null ? ConfigTypeRegistry.findAdapter(entry.getValue().getClass()) : null;
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
