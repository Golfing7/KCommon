package com.golfing8.kcommon.struct.region.ruled;

import lombok.Builder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * A rule that will prevent or allow combat between certain parties.
 */
@Builder
public class CombatRule extends RegionRule {
    private boolean allowCombat;
    private boolean allowCombatBetweenFriendlies;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPreDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
            return;

        if (allowCombatBetweenFriendlies)
            event.setCancelled(true);
        else
            event.setCancelled(!allowCombat);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPostDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player))
            return;

        if (allowCombatBetweenFriendlies) {
            event.setCancelled(false); // Remove cancellation
        }
    }
}
