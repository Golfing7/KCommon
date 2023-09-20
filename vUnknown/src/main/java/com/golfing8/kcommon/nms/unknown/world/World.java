package com.golfing8.kcommon.nms.unknown.world;

import com.golfing8.kcommon.nms.chunks.NMSChunkProvider;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSTileEntity;
import com.golfing8.kcommon.nms.unknown.chunks.ChunkProvider;
import com.golfing8.kcommon.nms.world.NMSWorld;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
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
    public void animateChest(NMSTileEntity nmsTileEntity, boolean opening) {
        throw new UnsupportedOperationException();
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
        return new Position(player.getTargetBlock((int) range));
    }

    @Override
    public void setTypeQuickly(Location location, Material material, byte b0) {
        location.getBlock().setType(material, false);
    }
}
