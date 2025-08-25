package com.golfing8.kcommon.nms.struct;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.*;

/**
 * Represents 'extra' data for an entity. Used specifically for server versions less than 1.13
 */
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor
public class EntityData {
    /**
     * The type of entity
     */
    private final EntityType entityType;
    /**
     * If a creeper is charged
     */
    private final boolean creeperCharged;
    /**
     * If a skeleton is a wither.
     */
    private final boolean skeletonWither;
    /**
     * If a horse is undead
     */
    private final boolean horseUndead;
    /**
     * If a horse is a skeleton
     */
    private final boolean horseSkeleton;

    @Override
    public String toString() {
        if (creeperCharged) {
            return "CHARGED_CREEPER";
        }

        if (NMS.getServerVersion().isAtOrAfter(NMSVersion.v1_13))
            return entityType.name();

        if (skeletonWither) {
            return "WITHER_SKELETON";
        } else if (horseUndead) {
            return "UNDEAD_HORSE";
        } else if (horseSkeleton) {
            return "SKELETON_HORSE";
        }
        return entityType.name();
    }

    /**
     * Converts the given string to an entity data instance.
     *
     * @param val the value.
     * @return the instance.
     */
    public static EntityData valueOf(String val) {
        if (val.equals("CHARGED_CREEPER")) {
            return EntityData.builder().entityType(EntityType.CREEPER).creeperCharged(true).build();
        }

        if (NMS.getServerVersion().isAtOrAfter(NMSVersion.v1_13))
            return EntityData.builder().entityType(EntityType.valueOf(val)).build();
        switch (val) {
            case "WITHER_SKELETON":
                return EntityData.builder().entityType(EntityType.SKELETON).skeletonWither(true).build();
            case "UNDEAD_HORSE":
                return EntityData.builder().entityType(EntityType.HORSE).horseUndead(true).build();
            case "SKELETON_HORSE":
                return EntityData.builder().entityType(EntityType.HORSE).horseSkeleton(true).build();
        }
        return EntityData.builder().entityType(EntityType.valueOf(val)).build();
    }

    /**
     * Creates an entity data instance from the given type of entity.
     *
     * @param type the type.
     * @return the entity data.
     */
    public static EntityData fromType(EntityType type) {
        return EntityData.builder().entityType(type).build();
    }

    /**
     * Loads an instance from the given entity.
     *
     * @return the instance.
     */
    public static EntityData fromEntity(Entity entity) {
        if (entity instanceof Creeper) {
            return EntityData.builder().entityType(entity.getType()).creeperCharged(((Creeper) entity).isPowered()).build();
        }

        if (NMS.getServerVersion().isAtOrAfter(NMSVersion.v1_13)) {
            return EntityData.builder().entityType(entity.getType()).build();
        }

        if (entity instanceof Skeleton) {
            return EntityData.builder().entityType(entity.getType()).skeletonWither(((Skeleton) entity).getSkeletonType() == Skeleton.SkeletonType.WITHER).build();
        } else if (entity instanceof Horse) {
            return EntityData.builder().entityType(entity.getType()).horseUndead(((Horse) entity).getVariant() == Horse.Variant.UNDEAD_HORSE)
                    .horseSkeleton(((Horse) entity).getVariant() == Horse.Variant.SKELETON_HORSE).build();
        }

        return EntityData.builder().entityType(entity.getType()).build();
    }
}
