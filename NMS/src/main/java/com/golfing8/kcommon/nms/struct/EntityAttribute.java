package com.golfing8.kcommon.nms.struct;

import com.google.common.collect.Sets;

import java.util.Set;

public enum EntityAttribute {
    GENERIC_MAX_HEALTH("MAX_HEALTH"),
    GENERIC_FOLLOW_RANGE("FOLLOW_RANGE"),
    GENERIC_KNOCKBACK_RESISTANCE("KB_RESISTANCE"),
    GENERIC_MOVEMENT_SPEED("MOVEMENT_SPEED"),
    GENERIC_FLYING_SPEED("FLYING_SPEED"),
    GENERIC_ATTACK_DAMAGE("ATTACK_DAMAGE"),
    GENERIC_ATTACK_KNOCKBACK("ATTACK_KNOCKBACK"),
    GENERIC_ATTACK_SPEED("ATTACK_SPEED"),
    GENERIC_ARMOR("ARMOR"),
    GENERIC_ARMOR_TOUGHNESS("ARMOR_TOUGHNESS"),
    GENERIC_FALL_DAMAGE_MULTIPLIER,
    GENERIC_LUCK("LUCK"),
    GENERIC_MAX_ABSORPTION,
    GENERIC_SAFE_FALL_DISTANCE,
    GENERIC_SCALE,
    GENERIC_STEP_HEIGHT,
    GENERIC_GRAVITY,
    GENERIC_JUMP_STRENGTH,
    GENERIC_BURNING_TIME,
    GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE,
    GENERIC_MOVEMENT_EFFICIENCY,
    GENERIC_OXYGEN_BONUS,
    GENERIC_WATER_MOVEMENT_EFFICIENCY,
    PLAYER_BLOCK_INTERACTION_RANGE,
    PLAYER_ENTITY_INTERACTION_RANGE,
    PLAYER_BLOCK_BREAK_SPEED,
    PLAYER_MINING_EFFICIENCY,
    PLAYER_SNEAKING_SPEED,
    PLAYER_SUBMERGED_MINING_SPEED,
    PLAYER_SWEEPING_DAMAGE_RATIO,
    HORSE_JUMP_STRENGTH,
    ZOMBIE_SPAWN_REINFORCEMENTS,
    ;

    Set<String> oldNames;
    EntityAttribute(String... names) {
        this.oldNames = Sets.newHashSet(names);
    }
    public static EntityAttribute byName(String name) {
        for (EntityAttribute value : values()) {
            try {
                return EntityAttribute.valueOf(name);
            } catch (IllegalArgumentException exc) {
                if (value.oldNames.contains(name))
                    return value;
            }
        }
        return null;
    }
}
