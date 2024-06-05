package com.golfing8.kcommon.nms.unknown.access;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.access.NMSMagicEntities;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MagicEntities implements NMSMagicEntities {
    private final WorldguardHook hook;

    @Override
    public GameProfile getGameProfile(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        return new GameProfile(profile.getId().toString(), profile.getName());
    }

    @Override
    public Giant spawnGiantWithAI(Location location) {
        return location.getWorld().spawn(location, Giant.class);
    }

    public Slime spawnSlimeWithSize(Location location, int size, double health){
        Slime slime = location.getWorld().spawn(location, Slime.class);
        slime.setSize(size);
        slime.setHealth(health);
        return slime;
    }

    public Monster spawnWitherSkeleton(Location location){
        return location.getWorld().spawn(location, WitherSkeleton.class);
    }

    public Guardian spawnElderGuardian(Location location){
        return location.getWorld().spawn(location, ElderGuardian.class);
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, EntityData data) {
        return (T) world.spawn(loc, data.getEntityType().getEntityClass(), (spawned) -> {
            if (data.isCreeperCharged())
                ((Creeper) spawned).setPowered(true);
        });
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz) {
        return loc.getWorld().spawn(loc, clazz);
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        return loc.getWorld().spawn(loc, clazz, reason);
    }

    @Override
    public void setLoadChunks(Entity entity, boolean value) {
        //Couldn't find any field to match the v1_8 equivalent :/
    }

    @Override
    public boolean canEntitySpawn(LivingEntity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        switch(attribute){
            case ATTACK_DAMAGE:
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(value);
                break;
            case MOVEMENT_SPEED:
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
                break;
            case KB_RESISTANCE:
                entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(value);
                break;
        }
    }

    @Override
    public void setPersists(Creature entity, boolean value) {
        entity.setPersistent(value);
    }

    @Override
    public void setKiller(LivingEntity entity, Player killer) {
        entity.setKiller(killer);
    }

    @Override
    public void setNoClip(Entity entity, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFromSpawner(Entity entity, boolean value) {
        setMobAI(entity, !value);
    }

    @Override
    public void setMobAI(Entity entity, boolean value) {
        Mob mob = (Mob) entity;
        mob.setAware(value);
    }
}
