package com.golfing8.kcommon.util;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatValue;
import com.hypixel.hytale.server.core.modules.entitystats.asset.DefaultEntityStatTypes;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import org.jetbrains.annotations.Nullable;

/**
 * Helpers for working with Hytale entities.
 */
public final class HytaleEntities {
    private HytaleEntities() {
    }

    /**
     * Fully heals the given entity
     *
     * @param ref the entity
     */
    public static void fullyHeal(Ref<EntityStore> ref) {
        if (!ref.isValid())
            return;

        Archetype<EntityStore> archetype = ref.getStore().getArchetype(ref);
        if (!archetype.contains(EntityStatMap.getComponentType()))
            return;

        EntityStatMap map = ref.getStore().getComponent(ref, EntityStatMap.getComponentType());
        if (map == null)
            return;

        EntityStatValue value = map.get(DefaultEntityStatTypes.getHealth());
        if (value == null)
            return;

        map.setStatValue(DefaultEntityStatTypes.getHealth(), value.getMax());
    }

    /**
     * Gets the entity attacker directly or indirectly from the given source
     *
     * @param source the source
     * @return the entity, or null if there is none or it is no longer valid
     */
    public static @Nullable Ref<EntityStore> getEntitySourceAttacker(@Nullable Damage.Source source) {
        // ProjectileSource extends EntitySource; its ref is already the shooter for legacy and 0.7.0 projectiles
        if (!(source instanceof Damage.EntitySource entitySource))
            return null;

        Ref<EntityStore> ref = entitySource.getRef();
        if (ref == null || !ref.isValid())
            return null;
        // Hytale uses the projectile itself as the source when the shooter is gone
        if (source instanceof Damage.ProjectileSource projectileSource && ref.equals(projectileSource.getProjectile()))
            return null;
        return ref;
    }
}
