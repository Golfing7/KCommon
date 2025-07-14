package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;

import java.util.Map;

/**
 * Represents a map that stores points in a chunk style format.
 * <p>
 * This allows for faster hashing with block positions.
 */
public interface ChunkStylePointMap<V> extends Map<Position, V> {
    ChunkColumn<V> getChunkColumn(Position position);

    ChunkColumn<V> getChunkColumn(int x, int z);
}
