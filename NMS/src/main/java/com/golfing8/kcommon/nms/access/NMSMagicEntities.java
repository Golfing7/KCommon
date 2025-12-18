package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

/**
 * NMS method access for entities
 */
public interface NMSMagicEntities {

    /**
     * Gets the game profile of the given player
     *
     * @param player the player
     * @return the game profile
     */
    GameProfile getGameProfile(Player player);

    /**
     * Spawns a giant with AI
     *
     * @param location the location
     * @return the giant spawned
     */
    Giant spawnGiantWithAI(Location location);

    //Initial health is required as slimes seem to override their health given if not done via NMS.
    /**
     * Spawns a slime with the given size
     *
     * @param location the location to spawn
     * @param size the size
     * @param initialHealth the initial health
     * @return the slime
     */
    Slime spawnSlimeWithSize(Location location, int size, double initialHealth);

    /**
     * Spawns a wither skeleton at the given location
     *
     * @param location the location
     * @return the wither skeleton
     */
    Monster spawnWitherSkeleton(Location location);

    /**
     * Spawns an elder guardian at the given location
     *
     * @param location the location
     * @return the elder location
     */
    Guardian spawnElderGuardian(Location location);

    /**
     * Checks if the entity can naturally spawn at the given location
     *
     * @param entity the entity
     * @return if it can spawn
     */
    boolean canEntitySpawn(LivingEntity entity);

    /**
     * Checks if the entity can fit at its current location
     *
     * @param entity the entity
     * @return true if it can fit
     */
    default boolean canEntityFit(Entity entity) {
        return canEntityFit(entity, entity.getLocation());
    }

    /**
     * Checks if the entity can fit at the location
     *
     * @param entity the entity
     * @param location the location
     * @return true if it can fit
     */
    boolean canEntityFit(Entity entity, Location location);

    /**
     * Sets the item in the entity's offhand
     *
     * @param entity the entity
     * @param stack the item
     */
    default void setItemInOffHand(LivingEntity entity, ItemStack stack) {
        entity.getEquipment().setItemInOffHand(stack);
    }

    /**
     * Sets the item's drop chance in the entity's offhand
     *
     * @param entity the entity
     * @param chance the chance
     */
    default void setItemInOffHandDropChance(LivingEntity entity, float chance) {
        entity.getEquipment().setItemInOffHandDropChance(chance);
    }

    /**
     * Gets the item in the entity's offhand
     *
     * @param entity the entity
     * @return the item
     */
    default ItemStack getItemInOffHand(LivingEntity entity) {
        return entity.getEquipment().getItemInOffHand();
    }

    /**
     * Gets the item's drop chance in the entity's offhand
     *
     * @param entity the entity
     * @return the chance
     */
    default float getItemInOffHandDropChance(LivingEntity entity) {
        return entity.getEquipment().getItemInOffHandDropChance();
    }

    /**
     * Sets the attribute on the given entity
     *
     * @param entity the entity
     * @param attribute the attribute
     * @param value the value
     */
    void setAttribute(LivingEntity entity, EntityAttribute attribute, double value);

    /**
     * Sets the entity's noclip flag
     *
     * @param entity entity
     * @param value flag value
     */
    void setNoClip(Entity entity, boolean value);

    /**
     * Sets the entity's 'from spawner' flag
     *
     * @param entity the entity
     * @param value flag value
     */
    void setFromSpawner(Entity entity, boolean value);

    /**
     * Sets if the mob has AI
     *
     * @param entity the entity
     * @param value if it has AI
     */
    void setMobAI(Entity entity, boolean value);

    /**
     * Sets if the entity forcefully loads chunks
     *
     * @param entity the entity
     * @param value if it loads chunks
     */
    void setLoadChunks(Entity entity, boolean value);

    /**
     * Sets the entity persist flag
     *
     * @param entity the entity
     * @param value flag value
     */
    void setPersists(Creature entity, boolean value);

    /**
     * Sets the killer of the given entity to the provided killer, or null.
     *
     * @param entity the entity.
     * @param killer the new killer.
     */
    void setKiller(LivingEntity entity, Player killer);

    /**
     * Spawns an entity
     *
     * @param world the world
     * @param loc the location
     * @param data the entity data
     * @return the spawned entity
     * @param <T> the entity type
     */
    default <T extends Entity> T spawnEntity(World world, Location loc, EntityData data) {
        return this.spawnEntity(world, loc, data, true);
    }

    /**
     * Spawns an entity
     *
     * @param world the world
     * @param loc the location
     * @param data the entity data
     * @param randomizeData if the entity has randomized equipment and data
     * @return the spawned entity
     * @param <T> the entity type
     */
    <T extends Entity> T spawnEntity(World world, Location loc, EntityData data, boolean randomizeData);

    /**
     * Spawns an entity
     *
     * @param world the world
     * @param loc the location
     * @param clazz the entity type
     * @return the spawned entity
     * @param <T> the entity type
     */
    <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz);

    /**
     * Spawns an entity
     *
     * @param world the world
     * @param loc the location
     * @param clazz the entity type
     * @param reason the reason the entity is spawning
     * @return the spawned entity
     * @param <T> the entity type
     */
    <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason);
}
