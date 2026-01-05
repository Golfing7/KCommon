package com.golfing8.kcommon.nms.unknown.worldguard;

import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Modern (7.x+) worldguard bindings
 */
public class Worldguard implements WorldguardHook {
    @Override
    public List<String> getRegions(Location location) {
        RegionManager regionManager =
                WorldGuard.getInstance().getPlatform().getRegionContainer().get(WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(location.getWorld().getName()));
        if (regionManager == null) return Lists.newArrayList();

        List<String> toReturn = Lists.newArrayList();

        for (ProtectedRegion region : regionManager.getApplicableRegions(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))) {
            toReturn.add(region.getId());
        }
        return toReturn;
    }

    @Override
    public boolean canPlayerBuild(Player player, Location at) {
        ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

        return protectionQuery.testBlockPlace(player, at, player.getInventory().getItemInMainHand().getType());
    }

    @Override
    public boolean canAttack(Player attacker, Player attacked) {
        ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

        return protectionQuery.testEntityDamage(attacker, attacked);
    }

    @Override
    public boolean canBeDamaged(Player player) {
        RegionManager regionManager =
                WorldGuard.getInstance().getPlatform().getRegionContainer().get(WorldGuard.getInstance().getPlatform().getMatcher().getWorldByName(player.getWorld().getName()));
        if (regionManager == null) return true;

        StateFlag invincibleFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("invincible");
        StateFlag pvpFlag = (StateFlag) WorldGuard.getInstance().getFlagRegistry().get("pvp");

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        ApplicableRegionSet applicableRegions = regionManager.getApplicableRegions(BlockVector3.at(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ()));
        return applicableRegions.queryState(localPlayer, invincibleFlag) != StateFlag.State.ALLOW && applicableRegions.queryState(localPlayer, pvpFlag) != StateFlag.State.DENY;
    }
}
