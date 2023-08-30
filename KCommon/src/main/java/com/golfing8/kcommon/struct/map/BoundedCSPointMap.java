package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.struct.region.CuboidRegion;
import com.golfing8.kcommon.struct.region.Region;
import com.golfing8.kcommon.nms.struct.Position;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 * A non-lazy bounded implementation of ChunkStylePointMap.
 *
 * Unlike the Unbounded version, this works with an implementation of a 2d array.
 * This improves speeds, but is more costly in terms of memory usage.
 * @param <V>
 */
public class BoundedCSPointMap<V> implements ChunkStylePointMap<V>{
    private final CuboidRegion region;

    private int maxXKeys, maxZKeys;

    //Our 2d map of hash columns.
    private Node[][] storedValues;

    private int size = 0;

    public BoundedCSPointMap(CuboidRegion region)
    {
        this.region = region;

        generateStorage(region);
    }

    @Override
    public int size() {
        return size;
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

        checkValidRegion(p);

        Node node = getCol(p);

        return (V) ((HashChunkColumn) node.value).get(p);
    }

    @Override
    public V put(Position key, V value) {
        Preconditions.checkNotNull(key, "Null keys not allowed!");
        checkValidRegion(key);

        Node node = getCol(key);

        Object add = ((HashChunkColumn) node.value).add(key, value);

        if(add == null)
            size++;

        return (V) add;
    }

    @Override
    public V remove(Object key) {
        Preconditions.checkNotNull(key, "Null keys not allowed!");

        Position p = (Position) key;

        checkValidRegion(p);

        Node n = getCol(p);

        HashChunkColumn column = (HashChunkColumn) n.value;

        V value = (V) column.remove(p);

        if(value != null)
            size--;

        return value;
    }

    @Override
    public void putAll(Map<? extends Position, ? extends V> m) {
        m.forEach(this::put);
    }

    @Override
    public void clear() {
        generateStorage(region);

        size = 0;
    }

    @Override
    public Set<Position> keySet() {
        throw new UnsupportedOperationException("Key set not supported!");
    }

    @Override
    public Collection<V> values() {
        Collection<V> toReturn = new LinkedList<>();

        for (int i = 0; i < maxXKeys; i++) {
            for (int j = 0; j < maxZKeys; j++) {
                toReturn.addAll(Lists.newArrayList(((HashChunkColumn) this.storedValues[i][j].value).iterator()));
            }
        }

        return toReturn;
    }

    @Override
    public Set<Entry<Position, V>> entrySet() {
        Set<Entry<Position, V>> entries = Sets.newHashSet();

        for (int i = 0; i < maxXKeys; i++) {
            for (int j = 0; j < maxZKeys; j++) {
                entries.addAll(((HashChunkColumn) this.storedValues[i][j].value).entries());
            }
        }

        return entries;
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

    private Node getCol(Position point)
    {
        return storedValues[xKey(point)][zKey(point)];
    }

    private void setCol(Position point, Node node)
    {
        storedValues[xKey(point)][zKey(point)] = node;
    }

    private int xKey(Position point)
    {
        return (point.getX() - (int) Math.floor(region.getMinimumXValue())) >> 4;
    }

    private int zKey(Position point)
    {
        return (point.getZ() - (int) Math.floor(region.getMinimumZValue())) >> 4;
    }

    private void checkValidRegion(Position point){
        Preconditions.checkArgument(point.getX() >= region.getMinimumXValue() &&
                point.getY() >= region.getMinimumYValue() &&
                point.getZ() >= region.getMinimumZValue() &&
                point.getX() <= region.getMaximumXValue() &&
                point.getY() <= region.getMaximumYValue() &&
                point.getZ() <= region.getMaximumZValue(), "Point does not lie within region!");
    }

    private void generateStorage(Region region)
    {
        maxXKeys = (Math.abs((int) Math.floor(region.getMinimumXValue()) - (int) Math.floor(region.getMaximumXValue())) >> 4) + 1;
        maxZKeys = (Math.abs((int) Math.floor(region.getMinimumZValue()) - (int) Math.floor(region.getMaximumZValue())) >> 4) + 1;

        storedValues = new Node[maxXKeys][];

        //Initialize
        for (int i = 0; i < maxXKeys; i++) {
            storedValues[i] = new Node[maxZKeys];

            for (int j = 0; j < maxZKeys; j++) {
                storedValues[i][j] = new Node(null, new HashChunkColumn<>());
            }
        }
    }

    private long createLongHash(Position p)
    {
        return ((long) (p.getX() >> 4) << 32) + (p.getZ() >> 4) - Integer.MIN_VALUE;
    }
}
