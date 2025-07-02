package com.golfing8.kcommon.nms.unknown.world;

import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.unknown.chunks.ChunkProvider;
import com.golfing8.kcommon.nms.world.NMSWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftChest;
import org.bukkit.entity.Player;

import java.util.Optional;

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

    private Optional<ChestBlockEntity> getChest(BlockPos pos) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockEntity block = level.getBlockEntity(pos);
        return block instanceof ChestBlockEntity chest ? Optional.of(chest) : Optional.empty();
    }

    @Override
    public void refreshChestState(Player player, Position position) {
        // We need to flip the state to get it to animate.
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(position.getX(), position.getY(), position.getZ());
        getChest(blockPos).ifPresent(chest -> {
            chest.openersCounter.openerAPICountChanged(level, blockPos, chest.getBlockState(), 0, chest.openersCounter.getOpenerCount());
        });
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
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(position.getX(), position.getY(), position.getZ());
        getChest(blockPos).ifPresent(chest -> {
            chest.openersCounter.openerAPICountChanged(level, blockPos, chest.getBlockState(), 0, chest.openersCounter.getOpenerCount() + 1);
            chest.openersCounter.opened = true;
        });
    }

    @Override
    public void forceChestClose(Position position) {
        ServerLevel level = ((CraftWorld) world).getHandle();
        BlockPos blockPos = new BlockPos(position.getX(), position.getY(), position.getZ());
        getChest(blockPos).ifPresent(chest -> {
            chest.openersCounter.openerAPICountChanged(level, blockPos, chest.getBlockState(), 0, 0);
            chest.openersCounter.opened = false;
        });
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
