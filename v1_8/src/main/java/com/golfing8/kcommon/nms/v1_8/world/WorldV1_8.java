package com.golfing8.kcommon.nms.v1_8.world;

import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.v1_8.chunks.ChunkProviderV1_8;
import com.golfing8.kcommon.nms.v1_8.tileentities.MobSpawnerV1_8;
import com.golfing8.kcommon.nms.v1_8.tileentities.TileEntityBeaconV1_8;
import com.golfing8.kcommon.nms.v1_8.tileentities.TileEntityContainerV1_8;
import com.golfing8.kcommon.nms.v1_8.tileentities.TileEntityV1_8;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class WorldV1_8 implements NMSWorld {
    private final WorldServer worldServer;

    public WorldV1_8(WorldServer worldServer){
        this.worldServer = worldServer;
    }

    @Override
    public Object getHandle() {
        return worldServer;
    }

    @Override
    public NMSChunkProvider getChunkProvider() {
        return new ChunkProviderV1_8(worldServer.chunkProviderServer);
    }

    @Override
    public int getMinHeight() {
        return 0;
    }

    public NMSTileEntity getTileEntity(Position position){
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());

        TileEntity atLocation = worldServer.getTileEntity(blockPosition);

        if(atLocation == null)
            return null;

        if(atLocation instanceof TileEntityMobSpawner)
            return new MobSpawnerV1_8((TileEntityMobSpawner) atLocation);
        else if(atLocation instanceof TileEntityBeacon)
            return new TileEntityBeaconV1_8((TileEntityBeacon) atLocation);
        else if(atLocation instanceof TileEntityContainer)
            return new TileEntityContainerV1_8((TileEntityContainer) atLocation);
        return new TileEntityV1_8(atLocation);
    }

    @Override
    public void animateChest(Position position, boolean opening) {
        BlockPosition blockposition = new BlockPosition(position.getX(),
                position.getY(),
                position.getZ());
        worldServer.playBlockAction(blockposition,
                worldServer.getType(blockposition).getBlock(),
                1,
                opening ? 1 : 0);
    }

    @Override
    public void forceChestOpen(Position position) {
        BlockPosition blockposition = new BlockPosition(position.getX(),
                position.getY(),
                position.getZ());
        TileEntity tileEntity = worldServer.getTileEntity(blockposition);
        if (!(tileEntity instanceof TileEntityChest)) {
            return;
        }

        TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
        if (tileEntityChest.l < 1000) {
            tileEntityChest.l += 1000;
        }
        worldServer.playBlockAction(blockposition,
                worldServer.getType(blockposition).getBlock(),
                1,
                tileEntityChest.l);
    }

    @Override
    public void forceChestClose(Position position) {
        BlockPosition blockposition = new BlockPosition(position.getX(),
                position.getY(),
                position.getZ());
        TileEntity tileEntity = worldServer.getTileEntity(blockposition);
        if (!(tileEntity instanceof TileEntityChest)) {
            return;
        }

        TileEntityChest tileEntityChest = (TileEntityChest) tileEntity;
        for (HumanEntity he : new ArrayList<>(tileEntityChest.getViewers())) {
            he.closeInventory();
        }
        tileEntityChest.l = 0;
        worldServer.playBlockAction(blockposition,
                worldServer.getType(blockposition).getBlock(),
                1,
                tileEntityChest.l);
    }

    @Override
    public void playEffect(Location location, String effect, int data) {
        location.getWorld().playEffect(location, Effect.valueOf(effect), data);
    }

    @Override
    public Position findTargetedBlock(Player player, double range) {
        Vec3D playerEyes = new Vec3D(player.getLocation().getX(), player.getLocation().getY() + player.getEyeHeight(), player.getLocation().getZ());

        Vector endPoint = player.getEyeLocation().getDirection().normalize().multiply(range);

        Vec3D end = new Vec3D(playerEyes.a + endPoint.getX(), playerEyes.b + endPoint.getY(), playerEyes.c + endPoint.getZ());

        MovingObjectPosition position = worldServer.rayTrace(playerEyes, end, false, false, false);

        if(position == null || position.type != MovingObjectPosition.EnumMovingObjectType.BLOCK)
            return null;

        return new Position(position.a().getX(), position.a().getY(), position.a().getZ());
    }

    @Override
    public void refreshBlockAt(Player player, Position position) {
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());

        PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(worldServer, blockPosition);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(change);
    }

    @Override
    public void setTypeQuickly(Location location, Material material, byte b0) {
        WorldServer server = ((CraftWorld) location.getWorld()).getHandle();

        //Load the chunk at the location.
        boolean before = server.chunkProviderServer.forceChunkLoad;
        server.chunkProviderServer.forceChunkLoad = true;

        server.chunkProviderServer.originalGetChunkAt(location.getBlockX() >> 4, location.getBlockZ() >> 4);

        if(WineSpigot.isWineSpigot()){
            this.worldServer.setTypeAndData(new BlockPosition(location.getX(), location.getY(), location.getZ()), CraftMagicNumbers.getBlock(material).fromLegacyData(b0), 2, false);
        }else{
            this.worldServer.setTypeAndData(new BlockPosition(location.getX(), location.getY(), location.getZ()), CraftMagicNumbers.getBlock(material).fromLegacyData(b0), 2);
        }

        server.chunkProviderServer.forceChunkLoad = before;
    }
}
