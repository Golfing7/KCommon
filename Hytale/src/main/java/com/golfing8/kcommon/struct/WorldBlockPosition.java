package com.golfing8.kcommon.struct;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import lombok.*;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import org.joml.Vector3i;

import java.util.Objects;

/**
 * A wrapper for a position that exists in a world
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorldBlockPosition implements CASerializable {
    private int x;
    private int y;
    private int z;
    private String world;

    /**
     * Converts this into a {@link BlockPosition} instance
     *
     * @return the block position
     */
    public @NonNullDecl BlockPosition toBlockPosition() {
        return new BlockPosition(x, y, z);
    }

    /**
     * Gets the name of the world for this position
     *
     * @return the world name
     */
    public @NonNullDecl String getWorldName() {
        return world;
    }

    /**
     * Gets the world for this position
     *
     * @return the world, or null if it isn't loaded
     */
    public @NullableDecl World getWorld() {
        return Universe.get().getWorld(world);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorldBlockPosition that = (WorldBlockPosition) o;
        return x == that.x && y == that.y && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, world);
    }

    /**
     * Constructs an instance from the given block position and world
     *
     * @param bp the block position
     * @param world the world
     * @return the world block position
     */
    public static @NonNullDecl WorldBlockPosition from(@NonNullDecl BlockPosition bp, @NonNullDecl World world) {
        return new WorldBlockPosition(bp.x, bp.y, bp.z, world.getName());
    }

    /**
     * Constructs an instance from the given vector and world
     *
     * @param vec the vector
     * @param world the world
     * @return the world block position
     */
    public static @NonNullDecl WorldBlockPosition from(@NonNullDecl Vector3i vec, @NonNullDecl World world) {
        return new WorldBlockPosition(vec.x, vec.y, vec.z, world.getName());
    }

    /**
     * Constructs an instance from the given vector and world
     *
     * @param vec the vector
     * @param world the world
     * @return the world block position
     */
    public static @NonNullDecl WorldBlockPosition from(@NonNullDecl com.hypixel.hytale.protocol.Vector3i vec, @NonNullDecl World world) {
        return new WorldBlockPosition(vec.x, vec.y, vec.z, world.getName());
    }
}
