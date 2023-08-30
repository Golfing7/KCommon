package com.golfing8.kcommon.struct.region.ruled;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BlockVector;

import java.util.List;
import java.util.Set;

/**
 * A rule that controls the entry and exit of parties.
 */
@Builder
public class EntryExitRule extends RegionRule {
    @Getter
    private Set<Player> allowEntry;
    @Getter
    private Set<Player> allowExit;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        BlockVector from = event.getFrom().toVector().toBlockVector();
        BlockVector to = event.getTo().toVector().toBlockVector();
        if (to.equals(from))
            return;

        boolean wasIn = getRegion().isPositionWithin(from);
        boolean isIn = getRegion().isPositionWithin(to);

        if (!isIn && wasIn) {
            if (allowExit != null && !allowExit.contains(event.getPlayer())) {
                event.setCancelled(true);
            }
        } else if (isIn && !wasIn) {
            if (allowEntry != null && !allowExit.contains(event.getPlayer())) {
                event.setCancelled(true);
            }
        }
    }
}
