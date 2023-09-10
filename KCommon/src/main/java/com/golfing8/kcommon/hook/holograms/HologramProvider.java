package com.golfing8.kcommon.hook.holograms;

import com.golfing8.kcommon.hook.holograms.dholograms.DHProvider;
import com.golfing8.kcommon.hook.holograms.hdisplays.HDProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public interface HologramProvider {
    /**
     * Creates a hologram at the specified location with the specified id.
     * @param location the location of the new hologram.
     * @return the created hologram.
     */
    Hologram createHologram(Location location);

    /**
     * Gets the global instance of the hologram provider.
     *
     * @return the instance.
     */
    static HologramProvider getInstance() {
        if (Bukkit.getServer().getPluginManager().isPluginEnabled("DecentHolograms"))
            return new DHProvider();
        else if (Bukkit.getServer().getPluginManager().isPluginEnabled("HolographicDisplays"))
            return new HDProvider();
        else
            return (location) -> new EmptyHologram();
    }
}
