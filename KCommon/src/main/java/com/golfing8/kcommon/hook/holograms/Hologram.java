package com.golfing8.kcommon.hook.holograms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a hologram, provides abstract access to simple methods of them.
 */
public interface Hologram {

    /**
     * True if this hologram is visible by default
     *
     * @return value
     */
    boolean isVisibleByDefault();

    /**
     * Sets if the hologram is visible by default
     *
     * @param visible true if visible
     */
    void setVisibleByDefault(boolean visible);

    /**
     * Sets this hologram as explicitly visible to the given player
     *
     * @param player the player to show
     */
    void setVisibleTo(Player player);

    /**
     * Removes the explicitly visible flag from the player
     *
     * @param player the player
     */
    void removeVisibleTo(Player player);

    /**
     * Sets this hologram as explicitly hidden to the given player
     *
     * @param player the player to hide
     */
    void setHideTo(Player player);

    /**
     * Removes the explicitly hide flag from the player
     *
     * @param player the player
     */
    void removeHideTo(Player player);

    /**
     * Sets a line in this hologram to the provided text at the specified index.
     *
     * @param index the index to set.
     * @param line  the line to set it to.
     */
    void setLine(int index, String line);

    /**
     * Sets a line in this hologram to the provided item at the specified index.
     *
     * @param index the index to set.
     * @param line  the line to set it to.
     */
    void setLine(int index, ItemStack line);

    /**
     * Removes a line from the hologram at the given index.
     *
     * @param index the index to remove.
     */
    void removeLine(int index);

    /**
     * Clears the lines on this hologram.
     */
    void clearLines();

    /**
     * Deletes this hologram.
     */
    void delete();

    /**
     * Checks if this hologram has been deleted and if it's still valid.
     *
     * @return true if the hologram is still valid.
     */
    boolean isDeleted();

    /**
     * Adds a line to the end of this hologram.
     *
     * @param line the line to add.
     */
    default void addLine(String line) {
        this.addLine(length(), line);
    }

    /**
     * Adds a line to the hologram at the specific index.
     *
     * @param index the index to add the line.
     * @param line  the line to add.
     */
    void addLine(int index, String line);

    /**
     * Adds a line to the end of this hologram.
     *
     * @param itemStack the line to add.
     */
    default void addLine(ItemStack itemStack) {
        this.addLine(length(), itemStack);
    }

    /**
     * Adds the line to the hologram at the specific index.
     *
     * @param index     the index to add the line.
     * @param itemStack the line to add.
     */
    void addLine(int index, ItemStack itemStack);

    /**
     * Sets the lines of this hologram.
     *
     * @param lines the lines.
     */
    void setLines(List<String> lines);


    /**
     * The length, in lines, of this hologram
     *
     * @return the length of the hologram.
     */
    int length();
}
