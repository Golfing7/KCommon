package com.golfing8.kcommon.nms.v1_17.world;

import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.v1_17.chunks.ChunkProviderV1_17;
import com.golfing8.kcommon.nms.v1_17.tileentities.MobSpawnerV1_17;
import com.golfing8.kcommon.nms.v1_17.tileentities.TileEntityBeaconV1_17;
import com.golfing8.kcommon.nms.v1_17.tileentities.TileEntityContainerV1_17;
import com.golfing8.kcommon.nms.v1_17.tileentities.TileEntityV1_17;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityBeacon;
import net.minecraft.world.level.block.entity.TileEntityContainer;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class WorldV1_17 implements NMSWorld {
    private final WorldServer worldServer;

    public WorldV1_17(WorldServer worldServer){
        this.worldServer = worldServer;
    }

    @Override
    public Object getHandle() {
        return worldServer;
    }

    @Override
    public NMSChunkProvider getChunkProvider() {
        return new ChunkProviderV1_17(worldServer.getChunkProvider());
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
            return new MobSpawnerV1_17((TileEntityMobSpawner) atLocation);
        else if(atLocation instanceof TileEntityBeacon)
            return new TileEntityBeaconV1_17((TileEntityBeacon) atLocation);
        else if(atLocation instanceof TileEntityContainer)
            return new TileEntityContainerV1_17((TileEntityContainer) atLocation);
        return new TileEntityV1_17(atLocation);
    }

    @Override
    public void animateChest(NMSTileEntity nmsTileEntity, boolean opening) {
        worldServer.playBlockAction(new BlockPosition(nmsTileEntity.getPosition().getX(),
                nmsTileEntity.getPosition().getY(),
                nmsTileEntity.getPosition().getZ()),
                (Block) nmsTileEntity.getBlock().getHandle(),
                1,
                opening ? 1 : 0);
    }

    @Override
    public void playEffect(Location location, String effect, int data) {
        try{
            location.getWorld().playEffect(location, Effect.valueOf(effect), data);
        }catch(IllegalArgumentException | NullPointerException exc){
            try{
                location.getWorld().spawnParticle(Particle.valueOf(effect), location, data);
            }catch(IllegalArgumentException | NullPointerException ignored){}
        }
    }

    @Override
    public Position findTargetedBlock(Player player, double range) {
        Vec3D playerEyes = new Vec3D(player.getLocation().getX(), player.getLocation().getY() + player.getEyeHeight(), player.getLocation().getZ());

        Vector endPoint = player.getEyeLocation().getDirection().normalize().multiply(range);

        Vec3D end = new Vec3D(playerEyes.getX() + endPoint.getX(), playerEyes.getY() + endPoint.getY(), playerEyes.getZ() + endPoint.getZ());

        MovingObjectPositionBlock positionBlock =
                worldServer.rayTrace(new RayTrace(playerEyes, end, RayTrace.BlockCollisionOption.c, RayTrace.FluidCollisionOption.a, ((CraftPlayer) player).getHandle()));

        if(positionBlock == null || positionBlock.getBlockPosition() == null || positionBlock.getType() == MovingObjectPosition.EnumMovingObjectType.a)
            return null;

        return new Position(positionBlock.getBlockPosition().getX(),
                positionBlock.getBlockPosition().getY(),
                positionBlock.getBlockPosition().getZ());
    }

    @Override
    public void refreshBlockAt(Player player, Position position) {
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());

        PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(worldServer, blockPosition);

        ((CraftPlayer) player).getHandle().b.sendPacket(change);
    }

    @Override
    public void setTypeQuickly(Location location, Material material, byte b0) {
        IBlockData data = CraftMagicNumbers.getBlock(material, b0);

        BlockPosition blockposition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData old = this.worldServer.getType(blockposition);
        boolean success = this.worldServer.setTypeAndData(blockposition, data, 1042);

        if (success) {
            this.worldServer.getMinecraftWorld().notify(blockposition, old, data, 3);
        }
    }
}
