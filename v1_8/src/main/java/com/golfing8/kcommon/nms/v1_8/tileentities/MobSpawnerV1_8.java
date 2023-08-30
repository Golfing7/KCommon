package com.golfing8.kcommon.nms.v1_8.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSMobSpawner;
import com.golfing8.kcommon.nms.v1_8.block.BlockV1_8;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.struct.Position;
import net.minecraft.server.v1_8_R3.TileEntityMobSpawner;
import org.bukkit.entity.EntityType;

public class MobSpawnerV1_8 implements NMSMobSpawner {
    private final TileEntityMobSpawner handle;

    public MobSpawnerV1_8(TileEntityMobSpawner handle){
        this.handle = handle;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public void setSpawnDelay(int delay) {
        handle.getSpawner().spawnDelay = delay;
    }

    @Override
    public int getSpawnDelay() {
        return handle.getSpawner().spawnDelay;
    }

    @Override
    public void setEntityType(EntityType entityType) {
        handle.getSpawner().setMobName(entityType.getName());
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.fromName(handle.getSpawner().getMobName());
    }

    @Override
    public Position getPosition() {
        return new Position(handle.getPosition().getX(), handle.getPosition().getY(), handle.getPosition().getZ());
    }

    @Override
    public NMSBlock getBlock() {
        return new BlockV1_8(handle.w());
    }
}
