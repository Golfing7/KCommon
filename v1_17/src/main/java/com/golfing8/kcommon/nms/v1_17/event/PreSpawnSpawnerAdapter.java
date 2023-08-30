package com.golfing8.kcommon.nms.v1_17.event;

import com.destroystokyo.paper.event.entity.PreSpawnerSpawnEvent;
import com.golfing8.kcommon.nms.event.PreSpawnSpawnerEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PreSpawnSpawnerAdapter implements Listener {

    @EventHandler
    public void onSpawn(PreSpawnerSpawnEvent event){
        PreSpawnSpawnerEvent preEvent = new PreSpawnSpawnerEvent(event.getSpawnerLocation().getBlock(), event.getType());

        Bukkit.getServer().getPluginManager().callEvent(preEvent);

        event.setCancelled(preEvent.isCancelled());
    }
}
