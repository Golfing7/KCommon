package com.golfing8.kcommon.struct.registry;

import net.kyori.adventure.key.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A registry storing values using a hashmap backing
 *
 * @param <T> the type stored
 */
public class HashRegistry<T> implements KRegistry<T> {
    private final Map<Key, T> backingMap = new HashMap<>();
    private final Map<String, T> byName = new HashMap<>();

    @Override
    public Collection<Map.Entry<Key, T>> entries() {
        return Collections.unmodifiableCollection(backingMap.entrySet());
    }

    @Override
    public Collection<T> elements() {
        return Collections.unmodifiableCollection(backingMap.values());
    }

    @Override
    public Optional<T> get(@NotNull Key key) {
        return Optional.ofNullable(backingMap.get(key));
    }

    @Override
    public Optional<T> getByName(@NotNull String key) {
        return Optional.ofNullable(byName.get(key));
    }

    @Override
    public T register(@NotNull Key key, @NotNull T value) {
        backingMap.put(key, value);
        byName.put(key.value(), value);
        return value;
    }

    @Override
    public @Nullable T unregister(@NotNull Key key) {
        T value = backingMap.remove(key);
        byName.remove(key.value());
        return value;
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return backingMap.values().iterator();
    }
}
