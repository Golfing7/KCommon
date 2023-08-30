package com.golfing8.kcommon.nms.v1_19.tileentities;

import com.golfing8.kcommon.nms.tileentities.NMSMobSpawner;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.entity.TileEntityMobSpawner;
import org.bukkit.entity.EntityType;

import java.util.Optional;

public class MobSpawner extends TileEntity implements NMSMobSpawner {
    private final TileEntityMobSpawner handle;

    public MobSpawner(TileEntityMobSpawner handle){
        super(handle);
        this.handle = handle;
    }

    @Override
    public Object getHandle() {
        return handle;
    }

    @Override
    public void setSpawnDelay(int delay) {
        handle.d().c = delay;
    }

    @Override
    public int getSpawnDelay() {
        return handle.d().c;
    }

    @Override
    public void setEntityType(EntityType entityType) {
        handle.d().a(EntityTypes.a(entityType.getName()).get());
    }

    @Override
    public EntityType getEntityType() {
        Optional<EntityTypes<?>> type = EntityTypes.a(handle.d().e.a());
        return type.isEmpty() ? EntityType.PIG : EntityType.fromName(EntityTypes.a(type.get()).a());
    }
}
