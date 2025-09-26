package com.golfing8.kcommon.util;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.struct.region.Region;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * A utility class for entities.
 */
@UtilityClass
public class EntityUtil {
    /**
     * Applies the consumer to the entity, its vehicles, and its passengers.
     * The consumer is applied from top down (highest passenger to the lowest vehicle).
     *
     * @param originalEntity the original entity.
     * @param consumer       the consumer
     */
    public static void applyToAllVehiclesAndPassengers(Entity originalEntity, Consumer<Entity> consumer) {
        applyToAllVehiclesAndPassengers(originalEntity, consumer, true);
        consumer.accept(originalEntity);
        applyToAllVehiclesAndPassengers(originalEntity, consumer, false);
    }

    private static void applyToAllVehiclesAndPassengers(Entity entity, Consumer<Entity> consumer, boolean directionUp) {
        if (directionUp && entity.getPassenger() != null) {
            applyToAllVehiclesAndPassengers(entity.getPassenger(), consumer, true);
            consumer.accept(entity.getPassenger());
        }

        if (!directionUp && entity.getVehicle() != null) {
            consumer.accept(entity.getVehicle());
            applyToAllVehiclesAndPassengers(entity.getVehicle(), consumer, false);
        }
    }

    /**
     * Gets all vehicles and passengers (including the original entity).
     *
     * @param originalEntity the original entity.
     * @return the list of all vehicles and passengers.
     */
    public static List<Entity> getAllVehiclesAndPassengers(Entity originalEntity) {
        List<Entity> entities = new ArrayList<>();
        applyToAllVehiclesAndPassengers(originalEntity, entities::add);
        return entities;
    }

    /**
     * Gets the top passenger entity.
     *
     * @param originalEntity the entity.
     * @return the top passenger entity.
     */
    public static Entity getTopEntity(Entity originalEntity) {
        if (originalEntity.getPassenger() != null)
            return getTopEntity(originalEntity.getPassenger());

        return originalEntity;
    }

    /**
     * Gets the bottom vehicle entity.
     *
     * @param originalEntity the entity.
     * @return the bottom vehicle entity.
     */
    public static Entity getBottomEntity(Entity originalEntity) {
        if (originalEntity.getVehicle() != null)
            return getTopEntity(originalEntity.getVehicle());

        return originalEntity;
    }

    /**
     * Gets the source of the given entity. If the entity is already a player, just that is returned.
     *
     * @param entity the entity.
     * @return the real player source
     */
    public static @Nullable Player getSource(Entity entity) {
        if (entity instanceof Player) {
            return (Player) entity;
        }
        if (entity instanceof Projectile) {
            if (((Projectile) entity).getShooter() instanceof Player)
                return (Player) ((Projectile) entity).getShooter();
        }
        return null;
    }

    /**
     * Gets a random position that the given region can spawn an entity on.
     *
     * @param region the region to look in
     * @return the spawnable mob position, or null if one couldn't be found.
     */
    public static Optional<Location> tryGetSpawnPosition(Region region) {
        // No world means that the region doesn't properly exist.
        if (region.getWorld() == null)
            return Optional.empty();

        search: for (int attempt = 0; attempt < 10; attempt++) {
            Location randomPosition = region.getRandomPosition().toLocation(region.getWorld());

            Block block = randomPosition.getBlock();
            if (!block.getType().isOccluding())
                continue;

            // Check for clear blocks above the block.
            for (int y = 1; y <= 2; y++) {
                block = block.getRelative(BlockFace.UP);
                if (!NMS.getTheNMS().getMagicBlocks().isPassable(block.getLocation()) || block.isLiquid())
                    continue search;
            }

            // We've found a position.
            return Optional.of(randomPosition.add(0.5, 1.0, 0.5));
        }
        return Optional.empty();
    }

    /**
     * Gets a random position that the given region can spawn an entity on.
     *
     * @param location the location to fix
     * @return the spawnable mob position, or null if one couldn't be found.
     */
    public static Optional<Location> tryFixSpawnLocation(Location location, int maxVariance) {
        // No world means that the region doesn't properly exist.
        if (location.getWorld() == null)
            return Optional.empty();

        Location workingLocation = location.clone();
        boolean occludingUp = false;
        int freeAirUp = 0;
        for (int i = 0; i < maxVariance; i++) {
            if (NMS.getTheNMS().getMagicBlocks().isPassable(workingLocation)) {
                freeAirUp++;
            } else {
                occludingUp = true;
                freeAirUp = 0;
            }

            if (occludingUp && freeAirUp >= 2)
                return Optional.of(workingLocation.subtract(0, 1, 0));

            workingLocation.add(0, 1, 0);
        }

        workingLocation = location.clone().add(0, 1, 0); // add 1 to allow for the mob to spawn at that level.
        boolean occludingDown = false;
        int freeAirDown = 0;
        for (int i = 0; i < maxVariance; i++) {
            if (NMS.getTheNMS().getMagicBlocks().isPassable(workingLocation)) {
                freeAirDown++;
                occludingDown = false;
            } else {
                occludingDown = true;
            }

            if (occludingDown && freeAirDown >= 2)
                return Optional.of(workingLocation.add(0, 1, 0));

            if (occludingDown)
                freeAirDown = 0;

            workingLocation.add(0, -1, 0);
        }
        return Optional.empty();
    }
}
