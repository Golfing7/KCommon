package com.golfing8.kcommon.listener;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.entity.EntityDefinition;
import com.golfing8.kcommon.util.EntityUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Consumer;

/**
 * A listener for handling linked entities.
 */
public class LinkedEntityListener implements Listener {
    private final NamespacedKey key = new NamespacedKey(KCommon.getInstance(), EntityDefinition.ENTITY_LINK_KEY);
    private final ThreadLocal<Boolean> onDeath_listen = ThreadLocal.withInitial(() -> true);
    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!onDeath_listen.get())
            return;

        if (!event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return;

        try {
            onDeath_listen.set(false);
            EntityUtil.applyToAllVehiclesAndPassengers(event.getEntity(), (entity) -> {
                if (entity != event.getEntity() && entity instanceof LivingEntity) {
                    ((LivingEntity) entity).setLastDamage(0.0D);
                    ((LivingEntity) entity).setNoDamageTicks(0);
                    ((LivingEntity) entity).damage(999999, event.getEntity().getKiller());
                }
            });
        } finally {
            onDeath_listen.set(true);
        }
    }

    private final ThreadLocal<Boolean> entityDamage_listen = ThreadLocal.withInitial(() -> true);
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!entityDamage_listen.get())
            return;

        // Apply damage across the entire mob.
        if (!event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING))
            return;

        if (!(event.getEntity() instanceof LivingEntity))
            return;

        try {
            entityDamage_listen.set(false);
            EntityUtil.applyToAllVehiclesAndPassengers(event.getEntity(), (entity) -> {
                if (entity != event.getEntity() && entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(event.getDamage(), event.getDamager());
                }
            });
        } finally {
            entityDamage_listen.set(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!entityDamage_listen.get())
            return;

        if (event instanceof EntityDamageByEntityEvent)
            return;

        try {
            entityDamage_listen.set(false);
            EntityUtil.applyToAllVehiclesAndPassengers(event.getEntity(), (entity) -> {
                if (entity != event.getEntity() && entity instanceof LivingEntity) {
                    ((LivingEntity) entity).damage(event.getDamage());
                }
            });
        } finally {
            entityDamage_listen.set(true);
        }
    }
}
