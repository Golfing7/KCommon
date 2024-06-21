package com.golfing8.kcommon.struct.entity;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.util.MS;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

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
public class EntityDefinition implements CASerializable {
    /** The entity type/data that defines the entity */
    @Builder.Default
    private EntityData type = EntityData.fromType(EntityType.PIG);
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
        Entity spawnedVehicle = vehicle != null ? vehicle.spawnEntity(location) : null;
        Entity selfSpawned = NMS.getTheNMS().getMagicEntities().spawnEntity(location.getWorld(), location, type, randomizeData);
        Entity spawnedPassenger = passenger != null ? passenger.spawnEntity(location) : null;

        if (selfSpawned instanceof LivingEntity) {
            if (maxHealth > 0) {
                ((LivingEntity) selfSpawned).setMaxHealth(maxHealth);
            }
            if (spawnHealth > 0) {
                ((LivingEntity) selfSpawned).setHealth(spawnHealth);
            }
            if (equipment != null) {
                equipment.apply((LivingEntity) selfSpawned);
            }
            if (potionEffects != null) {
                for (PotionEffect effect : potionEffects) {
                    ((LivingEntity) selfSpawned).addPotionEffect(effect);
                }
            }
        }

        if (selfSpawned instanceof Zombie) {
            ((Zombie) selfSpawned).setBaby(!adult);
        } else if (selfSpawned instanceof Ageable) {
            if (adult) {
                ((Ageable) selfSpawned).setAdult();
            } else {
                ((Ageable) selfSpawned).setBaby();
            }
        }

        if (spawnedVehicle != null) {
            spawnedVehicle.addPassenger(selfSpawned);
        }

        if (spawnedPassenger != null) {
            selfSpawned.addPassenger(spawnedPassenger);
        }

        if (name != null) {
            selfSpawned.setCustomNameVisible(true);
            selfSpawned.setCustomName(MS.parseSingle(name));
        }
        return selfSpawned;
    }
}
