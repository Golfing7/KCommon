package com.golfing8.kcommon.hook.holograms;

import com.golfing8.kcommon.hook.holograms.dholograms.DHProvider;
import com.golfing8.kcommon.hook.holograms.hdisplays.HDProvider;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Optional;

/**
 * An abstract provider for holograms
 */
public interface HologramProvider {
    HologramProvider EMPTY = new HologramProvider() {
        @Override
        public Hologram createHologram(Location location) {
            return new EmptyHologram();
        }

        @Override
        public Optional<Hologram> getById(String id) {
            return Optional.empty();
        }
    };

    /**
     * Creates a hologram at the specified location with the specified id.
     *
     * @param location the location of the new hologram.
     * @return the created hologram.
     */
    Hologram createHologram(Location location);

    /**
     * Creates a hologram at the specified location with the specified id.
     *
     * @param location the location of the new hologram.
     * @param id the id of the hologram
     * @return the created hologram.
     */
    default Hologram createHologram(Location location, String id) {
        return createHologram(location);
    }

    /**
     * Gets a hologram by its string id
     *
     * @param id the id
     * @return the optional hologram
     */
    Optional<Hologram> getById(String id);

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
            return EMPTY;
    }
}
