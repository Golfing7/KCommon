package com.golfing8.kcommon.nms.worldedit;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /**
     * Gets the selection for the given player.
     *
     * @param player the player
     * @return the player's wand selection
     */
    @NotNull
    Selection getSelection(Player player);

    @Data
    final class Selection {
        private final @Nullable Location pos1;
        private final @Nullable Location pos2;

        public boolean isComplete() {
            return pos1 != null && pos2 != null;
        }
    }

    WorldEditHook EMPTY = new WorldEditHook() {
        @Override
        public void pasteSchematic(Location location, Path path) {

        }

        @Override
        public @NotNull Selection getSelection(Player player) {
            return new Selection(null, null);
        }
    };
}
