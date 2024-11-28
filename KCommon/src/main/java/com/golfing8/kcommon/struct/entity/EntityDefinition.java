package com.golfing8.kcommon.struct.entity;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.struct.drop.DropTable;
import com.golfing8.kcommon.util.MS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a defined entity.
 * <p>
 * These entity definitions are meant to be records for entity spawn data.
 * </p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CASerializable.Options(canDelegate = true)
public class EntityDefinition implements CASerializable {
    public static final String ENTITY_LINK_KEY = "k_entity_link";

    private String _key;
    /** The entity type/data that defines the entity */
    @Builder.Default
    private EntityData entityType = EntityData.fromType(EntityType.PIG);
    /** The display name of this entity */
    private @Nullable String name;
    /** Any bukkit attributes that will be applied to this entity */
    private @Nullable Map<EntityAttribute, Double> attributes;
    /** Any effects that will be applied to the entity when they spawn */
    private @Nullable List<PotionEffect> potionEffects;
    /** Used to set the max health of the entity. If set to a positive number, this will override the MAX_HEALTH under {@link #attributes} */
    @Builder.Default
    private double maxHealth = -1;
    /** The amount of health the entity spawns with */
    @Builder.Default
    private double spawnHealth = -1;
    /** The equipment to apply to the entity */
    private @Nullable EntityEquipment equipment;
    /** The vehicle this entity can ride */
    private @Nullable EntityDefinition vehicle;
    /** The passenger of this entity */
    private @Nullable EntityDefinition passenger;
    /** If the entity should be the adult version */
    @Builder.Default
    private boolean adult = true;
    /** If normal spawn randomization should occur */
    private boolean randomizeData;
    /** If this entity will be linked to its vehicle and passenger in health and death time */
    private boolean linked;
    /** A drop table that MUST BE HANDLED BY THE USER. Simply spawning this entity WILL NOT override drops */
    private @Nullable DropTable dropTable;

    /**
     * Tries to spawn the entity at the given location.
     * <p>
     * If this entity has a {@link #vehicle}, it will be spawned at the given location and this entity will be added
     * as a passenger to it.
     * </p>
     *
     * @param location the location.
     * @return the spawned entity.
     */
    public Entity spawnEntity(Location location) {
        return spawnEntity(location, UUID.randomUUID());
    }

    private Entity spawnEntity(Location location, UUID spawnID) {
        Entity spawnedVehicle = vehicle != null ? vehicle.spawnEntity(location, spawnID) : null;
        Entity selfSpawned = NMS.getTheNMS().getMagicEntities().spawnEntity(location.getWorld(), location, entityType, randomizeData);
        Entity spawnedPassenger = passenger != null ? passenger.spawnEntity(location, spawnID) : null;

        applyToEntity(selfSpawned);
        if (spawnedVehicle != null) {
            spawnedVehicle.setCustomNameVisible(false);
            spawnedVehicle.setPassenger(selfSpawned);
        }

        if (spawnedPassenger != null) {
            selfSpawned.setCustomNameVisible(false);
            selfSpawned.setPassenger(spawnedPassenger);
        }

        if (linked) {
            if (!NMS.getTheNMS().supportsPersistentDataContainers()) {
                KCommon.getInstance().getLogger().warning("Entity definition " + _key + " set to link entities but server does not support persistent data containers!");
                return selfSpawned;
            }

            NamespacedKey namespacedKey = new NamespacedKey(KCommon.getInstance(), ENTITY_LINK_KEY);
            if (spawnedVehicle != null) {
                spawnedVehicle.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, spawnID.toString());
            }
            if (spawnedPassenger != null) {
                spawnedPassenger.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, spawnID.toString());
            }
            selfSpawned.getPersistentDataContainer().set(namespacedKey, PersistentDataType.STRING, spawnID.toString());
        }
        return selfSpawned;
    }

    /**
     * Tries to apply this definition to the entity.
     * The entity's type won't be changed.
     *
     * @param entity the entity to change.
     */
    public void applyToEntity(Entity entity) {
        if (entity instanceof LivingEntity) {
            if (maxHealth > 0) {
                ((LivingEntity) entity).setMaxHealth(maxHealth);
            }
            if (spawnHealth > 0) {
                ((LivingEntity) entity).setHealth(spawnHealth);
            }
            if (equipment != null) {
                equipment.apply((LivingEntity) entity);
            }
            if (potionEffects != null) {
                for (PotionEffect effect : potionEffects) {
                    ((LivingEntity) entity).addPotionEffect(effect);
                }
            }
        }

        if (entity instanceof Zombie) {
            ((Zombie) entity).setBaby(!adult);
        } else if (entity instanceof Ageable) {
            if (adult) {
                ((Ageable) entity).setAdult();
            } else {
                ((Ageable) entity).setBaby();
            }
        }

        if (name != null) {
            entity.setCustomNameVisible(true);
            entity.setCustomName(MS.parseSingle(name));
        }
    }

    /**
     * Gets the given entity's link key.
     *
     * @param entity the entity.
     * @return the entity link key.
     */
    public @Nullable UUID getEntityLinkKey(Entity entity) {
        if (!NMS.getTheNMS().supportsPersistentDataContainers())
            return null;

        NamespacedKey key = new NamespacedKey(KCommon.getInstance(), ENTITY_LINK_KEY);
        if (!entity.getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return null;

        return UUID.fromString(entity.getPersistentDataContainer().get(key, PersistentDataType.STRING));
    }
}
