package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.event.EcsEvents;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Builder;

/**
 * Controls players fighting each other inside the region.
 */
@Builder
public class CombatRule extends RegionRule {
    private boolean allowCombat;

    @Override
    protected void startEnforcing(CompositeTerminable enforcement) {
        if (allowCombat)
            return;

        bind(EcsEvents.listen(Damage.class, (entity, store, buffer, event) -> {
            if (!(event.getSource() instanceof Damage.EntitySource))
                return;

            PlayerRef victim = player(store, entity);
            PlayerRef attacker = player(store, ((Damage.EntitySource) event.getSource()).getRef());
            if (victim == null || attacker == null)
                return;

            if (getRegion().isWithin(victim) || getRegion().isWithin(attacker))
                event.setCancelled(true);
        }));
    }

    private static PlayerRef player(Store<EntityStore> store, Ref<EntityStore> ref) {
        return ref == null || !ref.isValid() ? null : store.getComponent(ref, PlayerRef.getComponentType());
    }
}
