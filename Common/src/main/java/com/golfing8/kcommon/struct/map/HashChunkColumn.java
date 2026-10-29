package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Stores values in a hash-like pattern in a vertical column.
 *
 * @param <V>
 */
public class HashChunkColumn<V> implements ChunkColumn<V> {
    private static final int CHUNK_SECTIONS = 24;
    private static final int RIGHT_BIT_SHIFT = 4;
    private static final int VALID_ARRAY_LOCATIONS = 16 * 16 * (384 / CHUNK_SECTIONS);

    private final int buckets;

    private final Node[][] storedObjects;

    private Node head, tail;

    private int size;

    public HashChunkColumn() {
        buckets = CHUNK_SECTIONS;

        storedObjects = new Node[buckets][];

        for (int i = 0; i < buckets; i++) {
            storedObjects[i] = new Node[VALID_ARRAY_LOCATIONS];
        }
    }

    @Override
    public Iterator<V> iterator() {
        return new NodeIterator<>(head);
    }

    @Override
    public V add(Position p, V v) {
        Preconditions.checkNotNull(p, "Point must not be null!");
        Preconditions.checkNotNull(v, "Null values not allowed!");
        Preconditions.checkArgument(checkValidLocation(p), "Point must be defined in y -64-320! Was " + p.getY());

        int hashKey = getHashKey(p);

        Node[] at = storedObjects[hashKey];

        int exactLocation = getExactLocationKey(p);

        Node exact = at[exactLocation];

        if (exact == null || exact.value == null) {
            Node newNode = new Node(p, v);

            newNode.previous = tail;
            if (tail != null)
                tail.next = newNode;
            else
                head = newNode;

            tail = newNode;

            at[exactLocation] = newNode;

            size++;
            return null;
        }

        V toReturn = (V) exact.value;

        //No need to relink, just update the value.
        exact.value = v;
        exact.key = p;
        return toReturn;
    }

    @Override
    public V remove(Position p) {
        Preconditions.checkNotNull(p, "Point must not be null!");
        Preconditions.checkArgument(checkValidLocation(p), "Point must be defined in y -64-320! Was " + p.getY());

        int hashKey = getHashKey(p);

        Node[] at = storedObjects[hashKey];

        int exactLocation = getExactLocationKey(p);

        Node exact = at[exactLocation];

        //No value anyway!
        if (exact == null || exact.value == null)
            return null;

        V toReturn = (V) exact.value;

        if (exact.next != null)
            exact.next.previous = exact.previous;
        if (exact.previous != null)
            exact.previous.next = exact.next;

        if (exact == head)
            head = exact.next;
        if (exact == tail)
            tail = exact.previous;

        at[exactLocation] = null;

        size--;
        return toReturn;
    }

    @Override
    public V get(Position p) {
        Preconditions.checkNotNull(p, "Point must not be null!");
        Preconditions.checkArgument(checkValidLocation(p), "Point must be defined in y -64-320! Was " + p.getY());

        Node exact = getAtExactLocation(p);

        return exact != null ? (V) exact.value : null;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Set<Map.Entry<Position, V>> entries() {
        Set<Map.Entry<Position, V>> entries = Sets.newHashSet();

        Node head = this.head;

        while (head != null) {
            entries.add(new UnboundedCSPointMap.MapEntry(head.key, head.value));

            head = head.next;
        }
        return entries;
    }

    /**
     * This implementation doesn't clear the backing array, but instead sets our head and tail to null, setting all values in the link to null.
     * This flags them as "dead" which allows us to overwrite them in the array.
     */
    @Override
    public void clear() {
        this.size = 0;

        Node head = this.head;

        this.head = this.tail = null;

        while (head != null) {
            head.value = null;

            head = head.next;
        }
    }

    private Node getAtExactLocation(Position p) {
        int hashKey = getHashKey(p);

        Node[] at = storedObjects[hashKey];

        int exactLocation = getExactLocationKey(p);

        return at[exactLocation];
    }

    private int getExactLocationKey(Position p) {
        int x = p.getX() & 15;
        int y = (p.getY() + 64) & 15;
        int z = p.getZ() & 15;

        return x << 8 | y << 4 | z;
    }

    private int getHashKey(Position p) {
        int y = (p.getY() + 64) >> RIGHT_BIT_SHIFT;

        if (y >= buckets || y < 0)
            throw new IllegalArgumentException("Point must be defined in y 1-256! Was " + p.getY());

        return y;
    }

    static class NodeIterator<V> implements Iterator<V> {
        private Node current;

        NodeIterator(Node current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public V next() {
            if (!hasNext())
                throw new NoSuchElementException();

            V value = (V) current.value;
            current = current.next;
            return value;
        }
    }
}
