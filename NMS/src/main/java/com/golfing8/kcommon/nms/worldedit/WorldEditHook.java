package com.golfing8.kcommon.nms.worldedit;

import org.bukkit.Location;

import java.io.File;
import java.nio.file.Path;

/**
 * A hook for world edit. Can be used to do things like set blocks or paste schematics.
 */
public interface WorldEditHook {

    /**
     * Paste the schematic at the given position.
     *
     * @param location the location to paste at.
     * @param path the path.
     */
    void pasteSchematic(Location location, Path path);
}
