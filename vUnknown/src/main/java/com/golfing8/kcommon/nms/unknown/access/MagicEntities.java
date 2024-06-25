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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

public class MagicEntities implements NMSMagicEntities {

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
    public <T extends Entity> T spawnEntity(World world, Location loc, EntityData data, boolean randomizeData) {
        return (T) world.spawn(loc, data.getEntityType().getEntityClass(), randomizeData, (spawned) -> {
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
        return canEntityFit(entity);
    }

    @Override
    public boolean canEntityFit(Entity entity, Location location) {
        BoundingBox box = entity.getBoundingBox().shift(location.clone().subtract(entity.getLocation()));
        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.max(Math.floor(box.getMinY()) - 1, entity.getWorld().getMinHeight());
        int minZ = (int) Math.floor(box.getMinZ());
        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.min(Math.floor(box.getMaxY()), entity.getWorld().getMaxHeight());
        int maxZ = (int) Math.floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = entity.getWorld().getBlockAt(x, y, z);
                    VoxelShape blockShape = block.getCollisionShape();
                    if (blockShape.overlaps(box))
                        return false;
                }
            }
        }
        return true;
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        AttributeInstance instance = null;
        switch(attribute){
            case ATTACK_DAMAGE:
                instance = entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
                break;
            case MOVEMENT_SPEED:
                instance = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                break;
            case KB_RESISTANCE:
                instance = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
                break;
            case MAX_HEALTH:
                instance = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                break;
            case LUCK:
                instance = entity.getAttribute(Attribute.GENERIC_LUCK);
                break;
            case ARMOR:
                instance = entity.getAttribute(Attribute.GENERIC_ARMOR);
                break;
            case ATTACK_SPEED:
                instance = entity.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                break;
            case FLYING_SPEED:
                instance = entity.getAttribute(Attribute.GENERIC_FLYING_SPEED);
                break;
            case FOLLOW_RANGE:
                instance = entity.getAttribute(Attribute.GENERIC_FOLLOW_RANGE);
                break;
            case ARMOR_TOUGHNESS:
                instance = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
                break;
            case ATTACK_KNOCKBACK:
                instance = entity.getAttribute(Attribute.GENERIC_ATTACK_KNOCKBACK);
                break;
            case HORSE_JUMP_STRENGTH:
                instance = entity.getAttribute(Attribute.HORSE_JUMP_STRENGTH);
                break;
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                instance = entity.getAttribute(Attribute.ZOMBIE_SPAWN_REINFORCEMENTS);
                break;
        }

        if (instance != null)
            instance.setBaseValue(value);
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
