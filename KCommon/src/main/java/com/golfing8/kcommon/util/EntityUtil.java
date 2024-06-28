package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;

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
        applyToAllVehiclesAndPassengers(originalEntity, consumer, false);

        consumer.accept(originalEntity);
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
}
