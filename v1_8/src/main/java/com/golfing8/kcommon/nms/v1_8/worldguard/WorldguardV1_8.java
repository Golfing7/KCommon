package com.golfing8.kcommon.nms.v1_8.worldguard;

import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.google.common.collect.Lists;
import com.sk89q.worldguard.bukkit.ProtectionQuery;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class WorldguardV1_8 implements WorldguardHook {

    private final WorldGuardPlugin plugin;

    public WorldguardV1_8() {
        this.plugin = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    @Override
    public List<String> getRegions(Location location) {
        List<String> all = Lists.newArrayList();
        for (ProtectedRegion region : plugin.getRegionManager(location.getWorld()).getApplicableRegions(location))
            all.add(region.getId());
        return all;
    }

    @Override
    public boolean canPlayerBuild(Player player, Location at) {
        ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

        return protectionQuery.testBlockPlace(player, at, player.getInventory().getItemInHand().getType());
    }

    @Override
    public boolean canAttack(Player attacker, Player attacked) {
        ProtectionQuery protectionQuery = WorldGuardPlugin.inst().createProtectionQuery();

        return protectionQuery.testEntityDamage(attacker, attacked);
    }

    @Override
    public boolean canBeDamaged(Player player) {
        StateFlag invincibleFlag = (StateFlag) plugin.getFlagRegistry().get("invincible");
        StateFlag pvpFlag = (StateFlag) plugin.getFlagRegistry().get("pvp");
        for (ProtectedRegion region : plugin.getRegionManager(player.getWorld()).getApplicableRegions(player.getLocation()))
            if (region.getFlag(invincibleFlag) == StateFlag.State.ALLOW || region.getFlag(pvpFlag) == StateFlag.State.DENY)
                return false;

        return true;
    }
}
