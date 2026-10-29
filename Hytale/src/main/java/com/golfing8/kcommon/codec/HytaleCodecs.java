package com.golfing8.kcommon.codec;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.CAHytaleCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.*;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3L;
import org.joml.Vector3i;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds the Hytale {@link Codec} of a type, so configs, data managers and {@link AutoCodec} can store Hytale types.
 * <p>
 * A type has a codec if one was {@link #register registered} for it, or if it declares a static {@code CODEC} field
 * holding one, as most of Hytale's own types do.
 * </p>
 */
public final class HytaleCodecs {
    /**
     * A location stored as its world, position and rotation.
     */
    public static final Codec<Location> LOCATION_CODEC = BuilderCodec.builder(Location.class, Location::new)
            .append(new KeyedCodec<>("World", Codec.STRING), Location::setWorld, Location::getWorld).add()
            .append(new KeyedCodec<>("Position", Vector3dUtil.CODEC), Location::setPosition, Location::getPosition).add()
            .append(new KeyedCodec<>("Rotation", Rotation3f.CODEC), Location::setRotation, Location::getRotation).add()
            .build();

    private static final Map<Class<?>, Codec<?>> REGISTERED = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Optional<Codec<?>>> FOUND = new ConcurrentHashMap<>();

    static {
        REGISTERED.put(Vector2d.class, Vector2dUtil.CODEC);
        REGISTERED.put(Vector2f.class, Vector2fUtil.CODEC);
        REGISTERED.put(Vector2i.class, Vector2iUtil.CODEC);
        REGISTERED.put(Vector3d.class, Vector3dUtil.CODEC);
        REGISTERED.put(Vector3f.class, Vector3fUtil.CODEC);
        REGISTERED.put(Vector3i.class, Vector3iUtil.CODEC);
        REGISTERED.put(Vector3L.class, Vector3LUtil.CODEC);
        REGISTERED.put(UUID.class, new UUIDStringCodec());
        REGISTERED.put(Component.class, new TextComponentCodec());
        REGISTERED.put(TextComponent.class, new TextComponentCodec());
        REGISTERED.put(Location.class, LOCATION_CODEC);
        // Items without extra data are stored as just their id.
        REGISTERED.put(ItemStack.class, new BetterItemCodec());
        REGISTERED.put(ZonedDateTime.class, new ZonedDateTimeCodec());
    }

    private HytaleCodecs() {
    }

    /**
     * Registers a codec for a type, used by data managers and {@link AutoCodec}, and as the type's config adapter.
     *
     * @param type  the type
     * @param codec the codec
     * @param <T>   the type
     */
    public static <T> void register(Class<T> type, Codec<T> codec) {
        REGISTERED.put(type, codec);
        FOUND.remove(type);
        ConfigTypeRegistry.registerAdapter(new CAHytaleCodec<>(type, codec));
    }

    /**
     * Finds the codec for a type.
     *
     * @param type the type
     * @param <T>  the type
     * @return the codec, or null if the type has none
     */
    @SuppressWarnings("unchecked")
    public static <T> @Nullable Codec<T> find(Class<T> type) {
        Codec<?> registered = REGISTERED.get(type);
        if (registered != null)
            return (Codec<T>) registered;

        return (Codec<T>) FOUND.computeIfAbsent(type, HytaleCodecs::findStaticCodec).orElse(null);
    }

    private static Optional<Codec<?>> findStaticCodec(Class<?> type) {
        try {
            Field field = type.getDeclaredField("CODEC");
            if (!Modifier.isStatic(field.getModifiers()) || !Codec.class.isAssignableFrom(field.getType()))
                return Optional.empty();

            field.setAccessible(true);
            return Optional.ofNullable((Codec<?>) field.get(null));
        } catch (NoSuchFieldException | IllegalAccessException exc) {
            return Optional.empty();
        }
    }
}
