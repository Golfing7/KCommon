package com.golfing8.kcommon.hook.holograms;

import org.bukkit.Location;

public interface HologramProvider {
    /**
     * Creates a hologram at the specified location with the specified id.
     * @param location the location of the new hologram.
     * @return the created hologram.
     */
    Hologram createHologram(Location location);
}
