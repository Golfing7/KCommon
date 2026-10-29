package com.golfing8.kcommon.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.common.util.StringUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Generates a Hytale {@link BuilderCodec} from the fields of a class, for ECS components, UI event data
 * and anything else Hytale needs a codec for.
 * <p>
 * Each non-static, non-transient field is stored under its capitalized name. Field types may be primitives, strings,
 * enums, lists, sets, string keyed maps, or any type {@link HytaleCodecs} has a codec for. A {@code _key} field is
 * filled with the key the object was stored under.
 * </p>
 */
public final class AutoCodec {
    private static final String KEY_FIELD_NAME = "_key";
    private static final Map<Type, Codec<?>> PRIMITIVE_CODECS = new HashMap<>();

    static {
        PRIMITIVE_CODECS.put(String.class, Codec.STRING);
        PRIMITIVE_CODECS.put(Boolean.class, Codec.BOOLEAN);
        PRIMITIVE_CODECS.put(boolean.class, Codec.BOOLEAN);
        PRIMITIVE_CODECS.put(Long.class, Codec.LONG);
        PRIMITIVE_CODECS.put(long.class, Codec.LONG);
        PRIMITIVE_CODECS.put(Integer.class, Codec.INTEGER);
        PRIMITIVE_CODECS.put(int.class, Codec.INTEGER);
        PRIMITIVE_CODECS.put(Short.class, Codec.SHORT);
        PRIMITIVE_CODECS.put(short.class, Codec.SHORT);
        PRIMITIVE_CODECS.put(Byte.class, Codec.BYTE);
        PRIMITIVE_CODECS.put(byte.class, Codec.BYTE);
        PRIMITIVE_CODECS.put(Double.class, Codec.DOUBLE);
        PRIMITIVE_CODECS.put(double.class, Codec.DOUBLE);
        PRIMITIVE_CODECS.put(Float.class, Codec.FLOAT);
        PRIMITIVE_CODECS.put(float.class, Codec.FLOAT);
    }

    private AutoCodec() {
    }

    /**
     * Generates a codec for the class.
     *
     * @param c       the class
     * @param factory the factory for the type
     * @param <T>     the type
     * @return the codec
     */
    public static <T> BuilderCodec<T> generateCodec(Class<T> c, Supplier<T> factory) {
        return generateCodec(c, factory, CodecSettings.builder(c).build());
    }

    /**
     * Generates a codec for the class that extends the parent's codec.
     *
     * @param c       the class
     * @param factory the factory for the type
     * @param parent  the parent class codec
     * @param <T>     the type
     * @return the codec
     */
    public static <T> BuilderCodec<T> generateCodec(Class<T> c, Supplier<T> factory, BuilderCodec<? super T> parent) {
        return generateCodec(c, factory, CodecSettings.builder(c).parent(parent).build());
    }

    /**
     * Generates a codec for the class.
     *
     * @param c             the class
     * @param factory       the factory for the type
     * @param codecSettings the settings
     * @param <T>           the type
     * @return the codec
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static <T> BuilderCodec<T> generateCodec(Class<T> c, Supplier<T> factory, CodecSettings<T> codecSettings) {
        BuilderCodec.BuilderBase codec = BuilderCodec.builder(c, factory, codecSettings.getParent());
        Field keyField = null;
        for (Field f : c.getDeclaredFields()) {
            f.setAccessible(true);
            // The key field is filled from where the object was stored, not from a value.
            if (f.getName().equals(KEY_FIELD_NAME)) {
                keyField = f;
                continue;
            }

            if ((f.getModifiers() & (Modifier.STATIC | Modifier.TRANSIENT)) != 0)
                continue;

            Codec<?> typeCodec = getCodec(f.getGenericType());
            if (typeCodec == null)
                throw new UnsupportedOperationException(String.format("No codec for field %s of type %s on class %s", f.getName(), f.getGenericType().getTypeName(), c.getName()));

            codec = codec.append(
                    new KeyedCodec<>(StringUtil.capitalize(f.getName(), ' '), typeCodec),
                    (inst, obj) -> set(f, inst, obj),
                    inst -> get(f, inst)
            ).add();
        }

        final Field finalKeyField = keyField;
        codec.afterDecode((obj, info) -> {
            ExtraInfo extraInfo = (ExtraInfo) info;
            // The object's own key is the last part of the path it was decoded at.
            String originalKey = extraInfo.peekKey();
            if (originalKey != null && finalKeyField != null)
                set(finalKeyField, obj, originalKey.substring(originalKey.lastIndexOf(".") + 1));

            if (codecSettings.getCallback() != null)
                codecSettings.getCallback().accept((T) obj, extraInfo);
        });
        return codec.build();
    }

    /**
     * Gets the codec for the given type.
     *
     * @param type the type
     * @return the codec, or null if there is none
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Codec<?> getCodec(Type type) {
        Codec<?> primitive = PRIMITIVE_CODECS.get(type);
        if (primitive != null)
            return primitive;

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) pType.getRawType();
            Type[] arguments = pType.getActualTypeArguments();
            if (List.class.isAssignableFrom(rawType)) {
                return new ListCodec<>((Codec) getCodec(arguments[0]), CopyOnWriteArrayList::new, false);
            } else if (Set.class.isAssignableFrom(rawType)) {
                return new SetCodec<>(getCodec(arguments[0]), () -> Collections.synchronizedSet(new HashSet<>()), false);
            } else if (Map.class.isAssignableFrom(rawType)) {
                // Map keys are always strings.
                return new MapCodec<>(getCodec(arguments[1]), ConcurrentHashMap::new, false);
            }
            return HytaleCodecs.find(rawType);
        }

        if (!(type instanceof Class))
            return null;

        Class<?> rawType = (Class<?>) type;
        Codec<?> found = HytaleCodecs.find(rawType);
        if (found != null)
            return found;
        return rawType.isEnum() ? new EnumCodec(rawType) : null;
    }

    private static void set(Field field, Object instance, Object value) {
        try {
            field.set(instance, value);
        } catch (IllegalAccessException exc) {
            throw new IllegalStateException("Failed to set field " + field.getName(), exc);
        }
    }

    private static Object get(Field field, Object instance) {
        try {
            return field.get(instance);
        } catch (IllegalAccessException exc) {
            throw new IllegalStateException("Failed to get field " + field.getName(), exc);
        }
    }
}
