package com.golfing8.kcommon.listener;

import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerContainer;
import com.golfing8.kcommon.data.SenderSerializable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.stream.Collectors;

/**
 * Listens for players quitting in order to save player data.
 * This only works for tracked (linked to modules) data managers.
 */
public class PlayerDataListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        for (DataManager<?> manager : DataManagerContainer.dataManagers.values().stream().flatMap(z -> z.values().stream()).collect(Collectors.toList())) {
            if (SenderSerializable.class.isAssignableFrom(manager.getDataClass())) {
                // Automatically save the data if it is associated with the player.
                manager.save(event.getPlayer().getUniqueId().toString());
            }
        }
    }
}
