package com.golfing8.kcommon.nms.v1_8.worldedit;

import com.boydti.fawe.object.schematic.Schematic;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.v1_8.NMS;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.ClipboardFormats;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

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

            new BukkitRunnable(){
                @Override
                public void run(){
                    NMSChunkProvider provider = nms.getWorld(location.getWorld()).getChunkProvider();

                    provider.setForceChunkLoad(true);

                    for(int x = schematic.getClipboard().getMinimumPoint().getBlockX() >> 4; x <= schematic.getClipboard().getMaximumPoint().getBlockX() >> 4; x++){
                        for(int z = schematic.getClipboard().getMinimumPoint().getBlockZ() >> 4; z <= schematic.getClipboard().getMaximumPoint().getBlockZ() >> 4; z++){
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
}
