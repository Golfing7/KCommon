package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public interface NMSMagicEntities {

    GameProfile getGameProfile(Player player);

    Giant spawnGiantWithAI(Location location);

    //Initial health is required as slimes seem to override their health given if not done via NMS.
    Slime spawnSlimeWithSize(Location location, int size, double initialHealth);

    Monster spawnWitherSkeleton(Location location);

    Guardian spawnElderGuardian(Location location);

    boolean canEntitySpawn(LivingEntity entity);

    default boolean canEntityFit(Entity entity) {
        return canEntityFit(entity, entity.getLocation());
    }

    boolean canEntityFit(Entity entity, Location location);

    default void setItemInOffHand(LivingEntity entity, ItemStack stack) {
        entity.getEquipment().setItemInOffHand(stack);
    }
    default void setItemInOffHandDropChance(LivingEntity entity, float chance) {
        entity.getEquipment().setItemInOffHandDropChance(chance);
    }

    default ItemStack getItemInOffHand(LivingEntity entity) {
        return entity.getEquipment().getItemInOffHand();
    }

    default float getItemInOffHandDropChance(LivingEntity entity) {
        return entity.getEquipment().getItemInOffHandDropChance();
    }

    void setAttribute(LivingEntity entity, EntityAttribute attribute, double value);

    void setNoClip(Entity entity, boolean value);

    void setFromSpawner(Entity entity, boolean value);

    void setMobAI(Entity entity, boolean value);

    void setLoadChunks(Entity entity, boolean value);

    void setPersists(Creature entity, boolean value);

    /**
     * Sets the killer of the given entity to the provided killer, or null.
     *
     * @param entity the entity.
     * @param killer the new killer.
     */
    void setKiller(LivingEntity entity, Player killer);

    <T extends Entity> T spawnEntity(World world, Location loc, EntityData data);

    <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz);

    <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason);
}
