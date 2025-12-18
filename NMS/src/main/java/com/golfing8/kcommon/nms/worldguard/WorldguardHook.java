package com.golfing8.kcommon.nms.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Contains abstract worldguard operations
 */
public interface WorldguardHook {

    /**
     * Gets the region IDs at the given location
     *
     * @param location the location
     * @return the regions
     */
    List<String> getRegions(Location location);

    /**
     * Checks if the player can build at the location
     *
     * @param player the player
     * @param at the location
     * @return if the player can build
     */
    boolean canPlayerBuild(Player player, Location at);

    /**
     * Checks if the player can attack the other player
     *
     * @param attacker the attacker
     * @param attacked the attacked
     * @return if the player can attack
     */
    boolean canAttack(Player attacker, Player attacked);

    /**
     * Checks if the player can be damaged
     *
     * @param player the player
     * @return if the player can be damaged
     */
    boolean canBeDamaged(Player player);

    WorldguardHook EMPTY = new WorldguardHook() {
        @Override
        public List<String> getRegions(Location location) {
            return Collections.emptyList();
        }

        @Override
        public boolean canPlayerBuild(Player player, Location at) {
            return true;
        }

        @Override
        public boolean canAttack(Player attacker, Player attacked) {
            return true;
        }

        @Override
        public boolean canBeDamaged(Player player) {
            return true;
        }
    };
}
