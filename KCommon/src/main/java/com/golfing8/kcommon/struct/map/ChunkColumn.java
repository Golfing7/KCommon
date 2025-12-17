package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;

import java.util.Map;
import java.util.Set;

/**
 * Represents an iterable of things stored in a chunk column style.
 * <p>
 * These locations MUST be defined in the real plane (1-256)Y, otherwise it will not work!
 *
 * @param <V>
 */
public interface ChunkColumn<V> extends Iterable<V> {

    /**
     * Adds a value at a point.
     *
     * @param p the point
     * @param v the value
     * @return the previous value
     */
    V add(Position p, V v);

    /**
     * Removes a value at a point
     *
     * @param p the point
     * @return the previous value
     */
    V remove(Position p);

    /**
     * Gets a value at a point
     *
     * @param p the point
     * @return the value
     */
    V get(Position p);

    /**
     * Gets the amount of values stored in this chunk column.
     *
     * @return the size of this chunk column
     */
    int size();

    /**
     * Gets the entries of this store.
     *
     * @return the entries.
     */
    Set<Map.Entry<Position, V>> entries();

    /**
     * Clears the values inside this column.
     */
    void clear();

    /**
     * Checks if the given position is valid in a world
     *
     * @param p the position
     * @return true if valid
     */
    default boolean checkValidLocation(Position p) {
        return p.getY() >= -64 && p.getY() <= 320;
    }
}
