package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Adapts instances of {@link Set}
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

        Type actualType = type.getGenericTypes().get(0);
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(actualType);
        Set toReturn = Reflection.instantiateOrGet(type.getType(), HashSet::new);
        Object obj = entry.unwrap();
        if (adapter != null) {
            // Perform useful macros.
            if (obj instanceof List) {
                List primitive = (List) obj;
                if (actualType instanceof Class && ((Class) actualType).isEnum() && !primitive.isEmpty() && primitive.get(0).toString().equals("@universe")) {
                    toReturn.addAll(Arrays.asList(((Class) actualType).getEnumConstants()));
                } else {
                    for (Object val : primitive) {
                        toReturn.add(adapter.toPOJO(ConfigPrimitive.ofTrusted(val), new FieldType(actualType)));
                    }
                }
            } else {
                if (actualType instanceof Class && ((Class) actualType).isEnum() && obj.toString().equals("@universe")) {
                    toReturn.addAll(Arrays.asList(((Class) actualType).getEnumConstants()));
                } else {
                    toReturn.add(adapter.toPOJO(ConfigPrimitive.ofTrusted(obj), new FieldType(actualType)));
                }
            }
            return toReturn;
        } else {
            if (obj instanceof List) {
                toReturn.addAll((List) obj);
            } else {
                toReturn.add(obj);
            }
        }
        return toReturn;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Set object) {
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
