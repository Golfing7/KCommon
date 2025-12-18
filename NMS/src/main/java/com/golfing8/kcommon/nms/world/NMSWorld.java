package com.golfing8.kcommon.nms.world;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * A wrapper for NMS worlds
 */
public interface NMSWorld extends NMSObject {
    /**
     * Gets the chunk provider
     *
     * @return the provider
     */
    NMSChunkProvider getChunkProvider();

    /**
     * Gets the minimum height of the world
     *
     * @return the height
     */
    int getMinHeight();

    /**
     * Refreshes the block at the position for the player
     *
     * @param player the player
     * @param position the position
     */
    void refreshBlockAt(Player player, Position position);

    /**
     * Sets the type at the location quickly
     *
     * @param location the location
     * @param material the material
     * @param b0 the data
     */
    void setTypeQuickly(Location location, Material material, byte b0);

    /**
     * Gets the tile entity at the given position
     *
     * @param position the position
     * @return the tile entity
     */
    NMSTileEntity getTileEntity(Position position);

    /**
     * Refreshes the state of the chest
     *
     * @param player the player
     * @param position the position
     */
    default void refreshChestState(Player player, Position position) {

    }

    /**
     * Animate the chest opening or closing
     *
     * @param position the position of the chest
     * @param opening open or close
     */
    void animateChest(Position position, boolean opening);

    /**
     * Forces the chest open at the location
     *
     * @param position the position
     */
    void forceChestOpen(Position position);

    /**
     * Forces the chest closed at the position
     *
     * @param position the position
     */
    void forceChestClose(Position position);

    /**
     * Plays an effect at the given location
     *
     * @param location the location
     * @param effect the effect
     * @param data the data
     */
    void playEffect(Location location, String effect, int data);

    /**
     * Find the player's targeted block position
     *
     * @param player the player
     * @param range the range
     * @return the targeted block
     */
    Position findTargetedBlock(Player player, double range);
}
