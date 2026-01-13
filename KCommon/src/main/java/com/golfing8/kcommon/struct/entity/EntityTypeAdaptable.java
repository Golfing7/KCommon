package com.golfing8.kcommon.struct.entity;

import com.cryptomorin.xseries.XEntityType;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.struct.Either;
import com.golfing8.kcommon.util.Reflection;
import io.lumine.mythic.api.MythicPlugin;
import io.lumine.mythic.api.MythicProvider;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

import java.util.Optional;

/**
 * An adaptable entity type for various types of entities provided from bukkit or mythic mobs
 */
@AllArgsConstructor
public class EntityTypeAdaptable {
    /** The bukkit or mythic mob entity type */
    private final Either<EntityData, String> bukkitOrMythicMobType;

    public EntityTypeAdaptable(XEntityType entityType) {
        this.bukkitOrMythicMobType = Either.left(EntityData.fromType(entityType.get()));
    }

    public EntityTypeAdaptable(String mythicMobsType) {
        this.bukkitOrMythicMobType = Either.right(mythicMobsType);
    }

    @Override
    public String toString() {
        return bukkitOrMythicMobType.left().map(EntityData::toString).orElse(bukkitOrMythicMobType.right().get());
    }

    /**
     * Spawns an entity at the given location
     *
     * @param location the location
     * @param randomizeData if the entity's data should be randomized
     * @return the entity
     */
    public Entity spawn(Location location, boolean randomizeData) {
        if (bukkitOrMythicMobType.left().isPresent()) {
            return NMS.getTheNMS().getMagicEntities().spawnEntity(location.getWorld(), location, bukkitOrMythicMobType.left().get(), randomizeData);
        } else if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            MythicPlugin plugin = MythicProvider.get();
            MythicMob mythicMob = plugin.getMobManager().getMythicMob(bukkitOrMythicMobType.right().orElseThrow(() -> new RuntimeException("MythicMob " + this + " not found"))).orElseThrow(() -> new RuntimeException("MythicMob " + this + " not found"));
            return mythicMob.spawn(BukkitAdapter.adapt(location), 1.0).getEntity().getBukkitEntity();
        }
        throw new IllegalStateException("Custom entity " + this + " cannot spawn without mythic mobs");
    }

    /**
     * Constructs an adaptable entity type from the given string
     *
     * @param type the type
     * @return the adaptable entity type
     */
    public static Optional<EntityTypeAdaptable> fromString(String type) {
        Optional<XEntityType> bukkitType = XEntityType.of(type);
        if (bukkitType.isPresent()) {
            return Optional.of(new EntityTypeAdaptable(Either.left(EntityData.fromType(bukkitType.get().get()))));
        } else if (Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            MythicPlugin pl = MythicProvider.get();
            Optional<MythicMob> mob = pl.getMobManager().getMythicMob(type);
            if (mob.isPresent()) {
                return Optional.of(new EntityTypeAdaptable(Either.right(type)));
            }
        }
        return Optional.empty();
    }
}
