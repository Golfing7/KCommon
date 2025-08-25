package com.golfing8.kcommon.nms.unknown.worldedit;

import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The 1.19 implementation for the worldedit hook.
 */
public class WorldEdit implements WorldEditHook {
    private final com.sk89q.worldedit.WorldEdit worldEdit;

    public WorldEdit() {
        this.worldEdit = com.sk89q.worldedit.WorldEdit.getInstance();
    }

    @Override
    public void pasteSchematic(Location location, Path path) {
        if (!Files.exists(path))
            return;

        try (EditSession session = worldEdit.newEditSession(new BukkitWorld(location.getWorld()))) {
            ClipboardFormat format = ClipboardFormats.findByFile(path.toFile());
            if (format == null)
                throw new RuntimeException("Clipboard format wasn't recognized!");

            ClipboardReader reader = format.getReader(Files.newInputStream(path));
            Clipboard clipboard = reader.read();

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .ignoreAirBlocks(true)
                    .build();

            Operations.complete(operation);
            session.commit();
        } catch (IOException | WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public @NotNull Selection getSelection(Player player) {
        WorldEditPlugin worldEditPlugin = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
        BukkitPlayer bukkitPlayer = worldEditPlugin.wrapPlayer(player);
        LocalSession session = worldEdit.getSessionManager().get(bukkitPlayer);
        RegionSelector selector = session.getRegionSelector(new BukkitWorld(player.getWorld()));
        if (!(selector instanceof CuboidRegionSelector cuboidSelector))
            return new Selection(null, null);

        CuboidRegion region = cuboidSelector.getIncompleteRegion();
        Location pos1 = region.getPos1() != null ?
                new Location(player.getWorld(), region.getPos1().getBlockX(), region.getPos1().getBlockY(), region.getPos1().getBlockZ()) : null;
        Location pos2 = region.getPos2() != null ?
                new Location(player.getWorld(), region.getPos2().getBlockX(), region.getPos2().getBlockY(), region.getPos2().getBlockZ()) : null;
        return new Selection(pos1, pos2);
    }
}
