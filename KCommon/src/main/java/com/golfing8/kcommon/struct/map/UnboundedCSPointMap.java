package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class UnboundedCSPointMap<V> implements ChunkStylePointMap<V>{
    private final Long2ObjectMap<ChunkColumn<V>> storedValues;

    public UnboundedCSPointMap()
    {
        this.storedValues = new Long2ObjectOpenHashMap<>(8096, 1.0F);
    }

    @Override
    public int size() {
        AtomicInteger accumulator = new AtomicInteger();

        storedValues.values().forEach(cc -> accumulator.addAndGet(cc.size()));
        return accumulator.get();
    }

    @Override
    public boolean isEmpty() {
        return size() > 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return get(key) != null;
    }

    @Deprecated
    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("Operation not supported on this chunk style map!");
    }

    @Override
    public V get(Object key) {
        Preconditions.checkNotNull(key, "Null keys not allowed!");

        Position p = (Position) key;

        long lKey = createLongHash(p);

        ChunkColumn<V> column = storedValues.get(lKey);

        return column != null ? storedValues.get(lKey).get(p) : null;
    }

    @Override
    public V put(Position key, V value) {
        Preconditions.checkNotNull(key, "Null keys not allowed!");

        long lKey = createLongHash(key);

        ChunkColumn<V> column = storedValues.computeIfAbsent(lKey, __ -> new HashChunkColumn<>());

        return column.add(key, value);
    }

    @Override
    public V remove(Object key) {
        Preconditions.checkNotNull(key, "Null keys not allowed!");

        Position p = (Position) key;

        long lKey = createLongHash(p);

        ChunkColumn<V> vs = storedValues.get(lKey);

        if(vs == null)
            return null;

        V toReturn = vs.remove(p);

        if(vs.size() == 0)
            storedValues.remove(lKey);

        return toReturn;
    }

    @Override
    public void putAll(Map<? extends Position, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        this.storedValues.values().forEach(ChunkColumn::clear);
    }

    @Override
    public Set<Position> keySet() {
        throw new UnsupportedOperationException("Key set not supported!");
    }

    @Override
    public Collection<V> values() {
        Collection<V> toReturn = new LinkedList<>();

        storedValues.values().forEach(cc -> {
            Iterator<V> iter = cc.iterator();

            iter.forEachRemaining(toReturn::add);
        });
        return toReturn;
    }

    @Override
    public Set<Entry<Position, V>> entrySet() {
        Set<Entry<Position, V>> entries = Sets.newHashSet();

        storedValues.long2ObjectEntrySet().forEach((obj) -> {
            entries.addAll(obj.getValue().entries());
        });

        return entries;
    }

    @Override
    public ChunkColumn<V> getChunkColumn(int x, int z) {
        return getChunkColumn(new Position(x * 16, 0, z * 16));
    }

    @Override
    public ChunkColumn<V> getChunkColumn(Position position) {
        return storedValues.get(createLongHash(position));
    }

    static class MapEntry<V> implements Entry<Position, V>
    {
        Position key;
        V value;

        MapEntry(Position key, V value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        public Position getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException("Operation not supported!");
        }
    }

    private long createLongHash(Position p)
    {
        return ((long) (p.getX() >> 4) << 32) + (p.getZ() >> 4) - Integer.MIN_VALUE;
    }
}
