package com.golfing8.kcommon.nms.v1_17.tileentities;

import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSMobSpawner;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.tileentities.NMSMobSpawner;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import org.bukkit.entity.EntityType;

public class MobSpawnerV1_17 extends TileEntityV1_17 implements NMSMobSpawner {
    private final TileEntityMobSpawner handle;

    public MobSpawnerV1_17(TileEntityMobSpawner handle){
        super(handle);
        this.handle = handle;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public void setSpawnDelay(int delay) {
        handle.getSpawner().d = delay;
    }

    @Override
    public int getSpawnDelay() {
        return handle.getSpawner().d;
    }

    @Override
    public void setEntityType(EntityType entityType) {
        handle.getSpawner().setMobName(EntityTypes.a(entityType.getName()).get());
    }

    @Override
    public EntityType getEntityType() {
        MinecraftKey key = handle.getSpawner().getMobName(handle.getWorld(), handle.getPosition());
        return key == null ? EntityType.PIG : EntityType.fromName(key.getKey());
    }

    @Override
    public Position getPosition() {
        return new Position(handle.getPosition().getX(), handle.getPosition().getY(), handle.getPosition().getZ());
    }
}
