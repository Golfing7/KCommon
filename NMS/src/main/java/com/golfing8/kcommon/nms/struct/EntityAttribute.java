package com.golfing8.kcommon.nms.struct;

import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Set;

/**
 * Version agnostic entity attributes
 */
public enum EntityAttribute {
    GENERIC_MAX_HEALTH("MAX_HEALTH", "generic.maxHealth"),
    GENERIC_FOLLOW_RANGE("FOLLOW_RANGE", "generic.followRange"),
    GENERIC_KNOCKBACK_RESISTANCE("KB_RESISTANCE", "generic.knockbackResistance"),
    GENERIC_MOVEMENT_SPEED("MOVEMENT_SPEED", "generic.movementSpeed"),
    GENERIC_FLYING_SPEED("FLYING_SPEED"),
    GENERIC_ATTACK_DAMAGE("ATTACK_DAMAGE", "generic.attackDamage"),
    GENERIC_ATTACK_KNOCKBACK("ATTACK_KNOCKBACK"),
    GENERIC_ATTACK_SPEED("ATTACK_SPEED"),
    GENERIC_ARMOR("ARMOR"),
    GENERIC_ARMOR_TOUGHNESS("ARMOR_TOUGHNESS"),
    GENERIC_FALL_DAMAGE_MULTIPLIER("FALL_DAMAGE_MULTIPLIER"),
    GENERIC_LUCK("LUCK"),
    GENERIC_MAX_ABSORPTION("MAX_ABSORPTION"),
    GENERIC_SAFE_FALL_DISTANCE("SAFE_FALL_DISTANCE"),
    GENERIC_SCALE("SCALE"),
    GENERIC_STEP_HEIGHT("STEP_HEIGHT"),
    GENERIC_GRAVITY("GRAVITY"),
    GENERIC_JUMP_STRENGTH("JUMP_STRENGTH"),
    GENERIC_BURNING_TIME("BURNING_TIME"),
    GENERIC_EXPLOSION_KNOCKBACK_RESISTANCE("EXPLOSION_KNOCKBACK_RESISTANCE"),
    GENERIC_MOVEMENT_EFFICIENCY("MOVEMENT_EFFICIENCY"),
    GENERIC_OXYGEN_BONUS("OXYGEN_BONUS"),
    GENERIC_WATER_MOVEMENT_EFFICIENCY("WATER_MOVEMENT_EFFICIENCY"),
    PLAYER_BLOCK_INTERACTION_RANGE("BLOCK_INTERACTION_RANGE"),
    PLAYER_ENTITY_INTERACTION_RANGE("ENTITY_INTERACTION_RANGE"),
    PLAYER_BLOCK_BREAK_SPEED("BLOCK_BREAK_SPEED"),
    PLAYER_MINING_EFFICIENCY("MINING_EFFICIENCY"),
    PLAYER_SNEAKING_SPEED("SNEAKING_SPEED"),
    PLAYER_SUBMERGED_MINING_SPEED("SUBMERGED_MINING_SPEED"),
    PLAYER_SWEEPING_DAMAGE_RATIO("SWEEPING_DAMAGE_RATIO"),
    HORSE_JUMP_STRENGTH("JUMP_STRENGTH"),
    ZOMBIE_SPAWN_REINFORCEMENTS("SPAWN_REINFORCEMENTS"),
    GRAVITY("GRAVITY"),
    ;

    @Getter
    final Set<String> oldNames;

    EntityAttribute(String... names) {
        this.oldNames = Sets.newHashSet(names);
    }

    /**
     * Gets an entity attribute by its name
     *
     * @param name the name
     * @return the attribute
     */
    public static EntityAttribute byName(String name) {
        try {
            return EntityAttribute.valueOf(name);
        } catch (IllegalArgumentException exc) {
            for (EntityAttribute value : values()) {
                if (value.oldNames.contains(name))
                    return value;
            }
        }
        return null;
    }
}
