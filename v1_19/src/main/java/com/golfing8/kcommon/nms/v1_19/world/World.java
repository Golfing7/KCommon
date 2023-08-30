package com.golfing8.kcommon.nms.v1_19.world;

import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.v1_19.chunks.ChunkProvider;
import com.golfing8.kcommon.nms.v1_19.tileentities.MobSpawner;
import com.golfing8.kcommon.nms.v1_19.tileentities.TileEntityBeacon;
import com.golfing8.kcommon.nms.v1_19.tileentities.TileEntityContainer;
import com.golfing8.kcommon.nms.v1_19.tileentities.TileEntity;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockChange;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.RayTrace;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.phys.MovingObjectPosition;
import net.minecraft.world.phys.MovingObjectPositionBlock;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class World implements NMSWorld {
    private final WorldServer worldServer;

    public World(WorldServer worldServer){
        this.worldServer = worldServer;
    }

    @Override
    public Object getHandle() {
        return worldServer;
    }

    @Override
    public NMSChunkProvider getChunkProvider() {
        return new ChunkProvider(worldServer.k());
    }

    @Override
    public int getMinHeight() {
        return worldServer.u_();
    }

    public NMSTileEntity getTileEntity(Position position){
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());

        net.minecraft.world.level.block.entity.TileEntity atLocation = worldServer.c_(blockPosition);

        if(atLocation == null)
            return null;

        if(atLocation instanceof TileEntityMobSpawner)
            return new MobSpawner((TileEntityMobSpawner) atLocation);
        else if(atLocation instanceof net.minecraft.world.level.block.entity.TileEntityBeacon)
            return new TileEntityBeacon((net.minecraft.world.level.block.entity.TileEntityBeacon) atLocation);
        else if(atLocation instanceof net.minecraft.world.level.block.entity.TileEntityContainer)
            return new TileEntityContainer((net.minecraft.world.level.block.entity.TileEntityContainer) atLocation);
        return new TileEntity(atLocation);
    }

    @Override
    public void animateChest(NMSTileEntity nmsTileEntity, boolean opening) {
        worldServer.a(new BlockPosition(nmsTileEntity.getPosition().getX(),
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
    public void refreshBlockAt(Player player, Position position) {
        BlockPosition blockPosition = new BlockPosition(position.getX(), position.getY(), position.getZ());

        PacketPlayOutBlockChange change = new PacketPlayOutBlockChange(worldServer, blockPosition);

        ((CraftPlayer) player).getHandle().b.a(change);
    }

    @Override
    public Position findTargetedBlock(Player player, double range) {
        Vec3D playerEyes = new Vec3D(player.getLocation().getX(), player.getLocation().getY() + player.getEyeHeight(), player.getLocation().getZ());

        Vector endPoint = player.getEyeLocation().getDirection().normalize().multiply(range);

        Vec3D end = new Vec3D(playerEyes.a() + endPoint.getX(), playerEyes.b() + endPoint.getY(), playerEyes.c() + endPoint.getZ());

        MovingObjectPositionBlock positionBlock =
                worldServer.a(new RayTrace(playerEyes, end, RayTrace.BlockCollisionOption.b, RayTrace.FluidCollisionOption.a, ((CraftPlayer) player).getHandle()));

        if(positionBlock.c() == MovingObjectPosition.EnumMovingObjectType.a)
            return null;

        return new Position(positionBlock.a().u(),
                positionBlock.a().v(),
                positionBlock.a().w());
    }

    @Override
    public void setTypeQuickly(Location location, Material material, byte b0) {
        IBlockData data = CraftMagicNumbers.getBlock(material, b0);

        BlockPosition blockposition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        IBlockData old = this.worldServer.a_(blockposition);
        boolean success = this.worldServer.a(blockposition, data, 1042);

        if (success) {
            this.worldServer.getMinecraftWorld().a(blockposition, old, data, 3);
        }
    }
}
