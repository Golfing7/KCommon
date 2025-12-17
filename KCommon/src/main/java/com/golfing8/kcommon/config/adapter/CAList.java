package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapts instances of {@link List}
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
        List toReturn = Reflection.instantiateOrGet(type.getType(), ArrayList::new);
        List primitive = entry.unwrap();
        if (adapter != null) {
            for (Object val : primitive) {
                toReturn.add(adapter.toPOJO(ConfigPrimitive.ofTrusted(val), new FieldType(actualType)));
            }
        } else {
            toReturn.addAll(primitive);
        }
        return toReturn;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull List object) {
        List<Object> primitives = new ArrayList<>();
        for (Object value : object) {
            Class<?> objectType = value.getClass();
            ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(objectType);
            if (adapter == null) {
                primitives.add(ConfigPrimitive.coerceObjectToBoxed(value, objectType));
            } else {
                primitives.add(adapter.toPrimitive(value).getPrimitive());
            }
        }
        return ConfigPrimitive.ofList(primitives);
    }
}
