package com.golfing8.kcommon.nms.v1_8.event;

import com.golfing8.kcommon.nms.event.PreSpawnSpawnerEvent;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * NMS 1.8 pre spawn event
 */
public class PreSpawnSpawnerAdapter implements Listener {

    @EventHandler
    public void onSpawn(SpawnerPreSpawnEvent event) {
        PreSpawnSpawnerEvent preEvent = new PreSpawnSpawnerEvent(event.getLocation().getBlock(), event.getSpawnedType());
        preEvent.setCancelled(event.isCancelled());

        Bukkit.getPluginManager().callEvent(preEvent);
        event.setCancelled(preEvent.isCancelled());
    }
}
