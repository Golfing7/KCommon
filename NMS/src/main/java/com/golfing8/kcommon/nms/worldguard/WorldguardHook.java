package com.golfing8.kcommon.nms.worldguard;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public interface WorldguardHook {

    List<String> getRegions(Location location);

    boolean canPlayerBuild(Player player, Location at);

    boolean canAttack(Player attacker, Player attacked);
    boolean canBeDamaged(Player player);
}
