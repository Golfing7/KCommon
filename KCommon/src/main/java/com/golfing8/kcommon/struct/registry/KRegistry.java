package com.golfing8.kcommon.struct.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * A registry of elements keyed by {@link Key}
 */
public interface KRegistry<T> extends Iterable<T> {
    /**
     * Gets all entries.
     *
     * @return the entries.
     */
    Collection<Map.Entry<Key, T>> entries();

    /**
     * Gets all elements registered to this registry.
     *
     * @return the elements.
     */
    Collection<T> elements();

    /**
     * Gets the element associated with the given key if it exists.
     *
     * @param key the key
     * @return the optional element
     */
    Optional<T> get(@NotNull Key key);

    /**
     * Gets the element associated with the given key. This represents only the {@link Key#key()} portion.
     *
     * @param key the key
     * @return the optional element
     */
    Optional<T> getByName(@NotNull String key);

    /**
     * Registers the given value under the given key
     *
     * @param key the key
     * @param value the value
     * @return value
     */
    T register(@NotNull Key key, @NotNull T value);

    /**
     * Unregisters the given key from the registry.
     *
     * @param key the key
     * @return the value
     */
    @Nullable T unregister(@NotNull Key key);
}
