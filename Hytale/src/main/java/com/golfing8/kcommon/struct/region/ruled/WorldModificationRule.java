package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.event.EcsEvents;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Builder;
import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * Stops blocks from being placed or broken inside the region.
 */
@Builder
public class WorldModificationRule extends RegionRule {
    private boolean allowBlockModifications;

    @Override
    protected void startEnforcing(CompositeTerminable enforcement) {
        if (allowBlockModifications)
            return;

        bind(EcsEvents.listen(BreakBlockEvent.class, (entity, store, buffer, event) -> {
            if (covers(store, event.getTargetBlock()))
                event.setCancelled(true);
        }));
        bind(EcsEvents.listen(PlaceBlockEvent.class, (entity, store, buffer, event) -> {
            if (covers(store, event.getTargetBlock()))
                event.setCancelled(true);
        }));
    }

    private boolean covers(Store<EntityStore> store, Vector3i block) {
        String world = getRegion().getWorld();
        if (world != null && !world.equals(store.getExternalData().getWorld().getName()))
            return false;

        return getRegion().isPositionWithin(new Vector3d(block.x, block.y, block.z));
    }
}
