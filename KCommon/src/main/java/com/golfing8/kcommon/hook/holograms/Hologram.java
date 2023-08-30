package com.golfing8.kcommon.hook.holograms;

/**
 * Represents a hologram, provides abstract access to simple methods of them.
 */
public interface Hologram {

    /**
     * Sets a line in this hologram to the provided text at the specified index.
     *
     * @param index the index to set.
     * @param line  the line to set it to.
     */
    void setLine(int index, String line);

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
     * The length, in lines, of this hologram
     *
     * @return the length of the hologram.
     */
    int length();
}
