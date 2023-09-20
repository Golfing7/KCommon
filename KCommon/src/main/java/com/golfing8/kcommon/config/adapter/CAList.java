package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An adapter for a list.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CAList implements ConfigAdapter<List> {
    @Override
    public Class<List> getAdaptType() {
        return List.class;
    }

    @Override
    public List toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return Collections.emptyList();

        Type actualType = type.getGenericTypes().get(0);
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(actualType);
        List primitive = (List) entry.unwrap();
        if (adapter != null) {
            List toReturn = new ArrayList();
            for (Object val : primitive) {
                toReturn.add(adapter.toPOJO(ConfigPrimitive.ofTrusted(val), new FieldType(actualType)));
            }
            return toReturn;
        }
        return primitive;
    }

    @Override
    public ConfigPrimitive toPrimitive(List object) {
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
