package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.blocks.WeightedCollection;
import com.golfing8.kcommon.struct.reflection.FieldType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * A config adapter for {@link WeightedCollection} instances.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CAWeightedCollection implements ConfigAdapter<WeightedCollection> {
    @Override
    public Class<WeightedCollection> getAdaptType() {
        return WeightedCollection.class;
    }

    @Override
    public WeightedCollection toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<?, Double> primitiveObject = (Map<?, Double>) entry.getPrimitive();
        WeightedCollection collection = new WeightedCollection();
        Type genericType = type.getGenericTypes().get(0);
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(genericType);
        primitiveObject.forEach((k, v) -> {
            if (adapter != null) {
                collection.addWeightedObject(adapter.toPOJO(ConfigPrimitive.ofTrusted(k), new FieldType(genericType)), v);
            } else {
                collection.addWeightedObject(k, v);
            }
        });

        return collection;
    }

    @Override
    public ConfigPrimitive toPrimitive(WeightedCollection object) {
        Map primitiveMap = new HashMap();
        object.getChanceMap().forEach((k, v) -> {
            Class<?> keyType = k.getClass();
            ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(keyType);
            if (adapter != null) {
                primitiveMap.put(adapter.toPrimitive(k).getPrimitive(), v);
            } else {
                primitiveMap.put(k, v);
            }
        });
        return ConfigPrimitive.ofMap(primitiveMap);
    }
}
