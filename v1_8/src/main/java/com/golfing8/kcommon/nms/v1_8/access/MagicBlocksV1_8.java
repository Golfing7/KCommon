package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicBlocks;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.AxisAlignedBB;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSkull;

/**
 * NMS 1.8 block access
 */
public class MagicBlocksV1_8 implements NMSMagicBlocks {
    private final FieldHandle<GameProfile> skullProfileField = FieldHandles.getHandle("profile", CraftSkull.class);

    @Override
    public void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer) {
        skullProfileField.set(skull, new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
    }

    @Override
    public boolean isPassable(Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData data = worldServer.getType(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        AxisAlignedBB bb = data.getBlock().a(worldServer, new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()), data);
        return bb == null || bb.a == bb.d && bb.b == bb.e && bb.c == bb.f;
    }
}
