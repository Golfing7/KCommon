package com.golfing8.kcommon.struct.region.ruled;

import lombok.Builder;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Hanging;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;

/**
 * A rule that controls the modification of the world.
 * <p>
 * This includes ALL modifications, such as:
 * </p>
 * <ol>
 * <li>Hanging entity placement and manipulation (Paintings, item frames)</li>
 * <li>Tile placement and destruction (Blocks)</li>
 * <li>Interaction of blocks (Opening doors)</li>
 * </ol>
 */
@Builder
public class WorldModificationRule extends RegionRule {
    private boolean allowBlockModifications;
    private boolean allowHangingEntityModification;

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!allowBlockModifications && getRegion().isPositionWithin(event.getBlock().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!allowBlockModifications && getRegion().isPositionWithin(event.getBlock().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        if (!allowHangingEntityModification && getRegion().isPositionWithin(event.getBlock().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (!allowHangingEntityModification && getRegion().isPositionWithin(event.getEntity().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onHangingInteract(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Hanging))
            return;

        if (!allowHangingEntityModification && getRegion().isPositionWithin(event.getRightClicked().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onArmorStandInteract(PlayerInteractAtEntityEvent event) {
        if (!(event.getRightClicked() instanceof ArmorStand))
            return;

        if (!allowHangingEntityModification && getRegion().isPositionWithin(event.getRightClicked().getLocation().toVector().toBlockVector()))
            event.setCancelled(true);
    }
}
