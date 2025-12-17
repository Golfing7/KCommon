package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;

import java.util.Map;

/**
 * Represents a map that stores points in a chunk style format.
 * <p>
 * This allows for faster hashing with block positions.
 */
public interface ChunkStylePointMap<V> extends Map<Position, V> {
    /**
     * Gets the chunk column associated with the given position
     *
     * @param position the position
     * @return the chunk column
     */
    ChunkColumn<V> getChunkColumn(Position position);

    /**
     * Gets the chunk column associated with the given x/z chunk coordinate
     *
     * @param x the x
     * @param z the z
     * @return the chunk column
     */
    ChunkColumn<V> getChunkColumn(int x, int z);
}
