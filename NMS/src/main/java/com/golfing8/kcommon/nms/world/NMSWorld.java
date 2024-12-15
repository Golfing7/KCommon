package com.golfing8.kcommon.nms.world;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface NMSWorld extends NMSObject {
    NMSChunkProvider getChunkProvider();

    int getMinHeight();

    void refreshBlockAt(Player player, Position position);

    void setTypeQuickly(Location location, Material material, byte b0);

    NMSTileEntity getTileEntity(Position position);

    default void refreshChestState(Player player, Position position) {}

    void animateChest(Position position, boolean opening);

    void forceChestOpen(Position position);
    void forceChestClose(Position position);

    void playEffect(Location location, String effect, int data);

    Position findTargetedBlock(Player player, double range);
}
