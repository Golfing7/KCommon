package com.golfing8.kcommon.nms.v1_8.worldedit;

import com.boydti.fawe.object.schematic.Schematic;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.v1_8.NMS;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * The 1.8 implementation for the worldedit hook.
 */
public class WorldEditV1_8 implements WorldEditHook {
    private final WorldEdit worldEdit;
    private final NMS nms;

    public WorldEditV1_8(NMS nms) {
        this.worldEdit = WorldEdit.getInstance();
        this.nms = nms;
    }

    @Override
    public void pasteSchematic(Location location, Path path) {
        BukkitWorld world = new BukkitWorld(location.getWorld());

        try {
            File file = path.toFile();

            Schematic schematic = ClipboardFormats.findByFile(file).load(file);

            Location actualPasteLocation = location.clone();
            actualPasteLocation.setX(location.getX() + schematic.getClipboard().getRegion().getCenter().getX());
            actualPasteLocation.setZ(location.getZ() + schematic.getClipboard().getRegion().getCenter().getZ());

            schematic.getClipboard().setOrigin(Vector.toBlockPoint(actualPasteLocation.getX(),
                    actualPasteLocation.getY(),
                    actualPasteLocation.getZ()));

            schematic.paste((com.sk89q.worldedit.world.World) world, Vector.toBlockPoint(location.getX(), location.getY(), location.getZ()));

            new BukkitRunnable() {
                @Override
                public void run() {
                    NMSChunkProvider provider = nms.getWorld(location.getWorld()).getChunkProvider();

                    provider.setForceChunkLoad(true);

                    for (int x = schematic.getClipboard().getMinimumPoint().getBlockX() >> 4; x <= schematic.getClipboard().getMaximumPoint().getBlockX() >> 4; x++) {
                        for (int z = schematic.getClipboard().getMinimumPoint().getBlockZ() >> 4; z <= schematic.getClipboard().getMaximumPoint().getBlockZ() >> 4; z++) {
                            location.getWorld().refreshChunk(x, z);
                        }
                    }

                    provider.setForceChunkLoad(false);
                }
            }.runTask(nms.getPlugin());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull Selection getSelection(Player player) {
        WorldEditPlugin worldEditPlugin = WorldEditPlugin.getPlugin(WorldEditPlugin.class);
        BukkitPlayer bukkitPlayer = worldEditPlugin.wrapPlayer(player);
        LocalSession session = worldEdit.getSessionManager().get(bukkitPlayer);
        RegionSelector selector = session.getRegionSelector(new BukkitWorld(player.getWorld()));
        if (!(selector instanceof CuboidRegionSelector))
            return new Selection(null, null);

        CuboidRegionSelector cuboidSelector = (CuboidRegionSelector) selector;
        Location pos1 = cuboidSelector.position1 != null ?
                new Location(player.getWorld(), cuboidSelector.position1.getBlockX(), cuboidSelector.position1.getBlockY(), cuboidSelector.position1.getBlockZ()) : null;
        Location pos2 = cuboidSelector.position2 != null ?
                new Location(player.getWorld(), cuboidSelector.position2.getBlockX(), cuboidSelector.position2.getBlockY(), cuboidSelector.position2.getBlockZ()) : null;
        return new Selection(pos1, pos2);
    }
}
