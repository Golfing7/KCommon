package com.golfing8.kcommon.nms.v1_8.event;

import com.golfing8.kcommon.nms.event.PreSpawnSpawnerEvent;
import net.techcable.tacospigot.event.entity.SpawnerPreSpawnEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PreSpawnSpawnerAdapter implements Listener {

    @EventHandler
    public void onSpawn(SpawnerPreSpawnEvent event){
        PreSpawnSpawnerEvent preEvent = new PreSpawnSpawnerEvent(event.getLocation().getBlock(), event.getSpawnedType());

        Bukkit.getPluginManager().callEvent(preEvent);

        event.setCancelled(preEvent.isCancelled());
    }
}
