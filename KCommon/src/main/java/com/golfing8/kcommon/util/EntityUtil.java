package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;

import java.util.ArrayList;
import java.util.List;
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
     * @param consumer the consumer
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
}
