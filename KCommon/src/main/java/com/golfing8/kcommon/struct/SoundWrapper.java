package com.golfing8.kcommon.struct;

import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.KCommon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A wrapper containing a sound to send to a player.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SoundWrapper {
    private final XSound sound;
    private final float volume;
    private final float pitch;
    private int delay = 0;

    /**
     * Sends the sound contained by this wrapper to the player.
     *
     * @param player the player.
     */
    public void send(Player player) {
        if(delay > 0) {
            Bukkit.getServer().getScheduler().runTaskLater(KCommon.getInstance(), () -> {
                player.playSound(player.getLocation(), sound.parseSound(), volume, pitch);
            }, delay);
        }else {
            player.playSound(player.getLocation(), sound.parseSound(), volume, pitch);
        }
    }

    /**
     * Plays the sound at the given location.
     *
     * @param location the location to play the sound.
     */
    public void send(Location location) {
        if (delay > 0) {
            Bukkit.getServer().getScheduler().runTaskLater(KCommon.getInstance(), () -> {
                location.getWorld().playSound(location, sound.parseSound(), volume, pitch);
            }, delay);
        }else {
            location.getWorld().playSound(location, sound.parseSound(), volume, pitch);
        }
    }
}
