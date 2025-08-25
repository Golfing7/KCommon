package com.golfing8.kcommon.nms.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public interface WorldguardHook {

    List<String> getRegions(Location location);

    boolean canPlayerBuild(Player player, Location at);

    boolean canAttack(Player attacker, Player attacked);

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
