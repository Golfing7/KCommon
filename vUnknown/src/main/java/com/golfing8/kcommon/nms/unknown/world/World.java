package com.golfing8.kcommon.nms.unknown.world;

import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.unknown.chunks.ChunkProvider;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.entity.Player;

public class World implements NMSWorld {
    private final org.bukkit.World world;

    public World(org.bukkit.World worldServer){
        this.world = worldServer;
    }

    @Override
    public Object getHandle() {
        return world;
    }

    @Override
    public NMSChunkProvider getChunkProvider() {
        return new ChunkProvider(world);
    }

    @Override
    public int getMinHeight() {
        return world.getMinHeight();
    }

    public NMSTileEntity getTileEntity(Position position){
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshChestState(Player player, Position position) {
        Location location = position.toLocation(world);

        // We need to flip the state to get it to animate.
        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        ChestBlockEntity chest = ((CraftChest) world.getBlockAt(location).getState()).getBlockEntity();
        chest.openersCounter.openerAPICountChanged(level, new BlockPos(position.getX(), position.getY(), position.getZ()), chest.getBlockState(), 0, chest.openersCounter.getOpenerCount());
    }

    @Override
    public void animateChest(Position position, boolean opening) {
        if (opening) {
            forceChestOpen(position);
        } else {
            forceChestClose(position);
        }
    }

    @Override
    public void forceChestOpen(Position position) {
        Location location = position.toLocation(world);

        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        ChestBlockEntity chest = ((CraftChest) location.getBlock().getState()).getBlockEntity();
        chest.openersCounter.openerAPICountChanged(level, new BlockPos(position.getX(), position.getY(), position.getZ()), chest.getBlockState(), 0, chest.openersCounter.getOpenerCount() + 1);
        chest.openersCounter.opened = true;
    }

    @Override
    public void forceChestClose(Position position) {
        Location location = position.toLocation(world);

        ServerLevel level = ((CraftWorld) location.getWorld()).getHandle();
        ChestBlockEntity chest = ((CraftChest) location.getBlock().getState()).getBlockEntity();
        chest.openersCounter.openerAPICountChanged(level, new BlockPos(position.getX(), position.getY(), position.getZ()), chest.getBlockState(), 0, 0);
        chest.openersCounter.opened = false;
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
        player.getWorld().refreshChunk(position.getX() >> 4, position.getZ() >> 4);
    }

    @Override
    public Position findTargetedBlock(Player player, double range) {
        return new Position(player.getTargetBlock(null, (int) range));
    }

    @Override
    public void setTypeQuickly(Location location, Material material, byte b0) {
        location.getBlock().setType(material, false);
    }
}
