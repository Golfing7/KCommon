package com.golfing8.kcommon.hook.protection;

import com.golfing8.kcommon.nms.struct.Position;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Collections;
import java.util.List;

/**
 * Asks the server's region protection plugin what players may do. The Hytale counterpart of the Bukkit
 * {@code WorldguardHook}; backed by OrbisGuard when it is installed.
 */
public interface ProtectionHook {
    /**
     * Gets the ids of the protected regions at the position.
     *
     * @param world    the world name.
     * @param position the position.
     * @return the region ids.
     */
    List<String> getRegions(String world, Position position);

    /**
     * Checks if the player may build at the position.
     *
     * @param player   the player.
     * @param world    the world name.
     * @param position the position.
     * @return true if building is allowed.
     */
    boolean canPlayerBuild(PlayerRef player, String world, Position position);

    /**
     * Checks if one player may attack another where the attacked player stands.
     *
     * @param attacker the attacker.
     * @param attacked the attacked player.
     * @return true if PvP is allowed.
     */
    boolean canAttack(PlayerRef attacker, PlayerRef attacked);

    /**
     * Checks if the player can take damage where they stand.
     *
     * @param player the player.
     * @return true if the player can be damaged.
     */
    boolean canBeDamaged(PlayerRef player);

    /**
     * A hook that allows everything, used when no protection plugin is installed.
     */
    ProtectionHook EMPTY = new ProtectionHook() {
        @Override
        public List<String> getRegions(String world, Position position) {
            return Collections.emptyList();
        }

        @Override
        public boolean canPlayerBuild(PlayerRef player, String world, Position position) {
            return true;
        }

        @Override
        public boolean canAttack(PlayerRef attacker, PlayerRef attacked) {
            return true;
        }

        @Override
        public boolean canBeDamaged(PlayerRef player) {
            return true;
        }
    };
}
