package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;

import java.util.*;

/**
 * An adapter for a set.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CASet implements ConfigAdapter<Set> {
    @Override
    public Class<Set> getAdaptType() {
        return Set.class;
    }

    @Override
    public Set toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Collections.emptySet();

        Class<?> actualType = type.getGenericTypes().get(0);
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(actualType);
        Set toReturn = new HashSet();
        if (adapter != null) {
            for (Object val : (List) entry.getPrimitive()) {
                toReturn.add(adapter.toPOJO(ConfigPrimitive.ofTrusted(val), new FieldType(actualType)));
            }
            return toReturn;
        }
        toReturn.addAll((List) entry.getPrimitive());
        return toReturn;
    }

    @Override
    public ConfigPrimitive toPrimitive(Set object) {
        List<Object> primitives = new ArrayList<>();
        for (Object value : object) {
            Class<?> objectType = value.getClass();
            ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(objectType);
            if (adapter == null) {
                primitives.add(value);
            } else {
                primitives.add(adapter.toPrimitive(value).getPrimitive());
            }
        }
        return ConfigPrimitive.ofList(primitives);
    }
}
