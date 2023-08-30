package com.golfing8.kcommon.hook.holograms.dholograms;

import com.golfing8.kcommon.hook.holograms.Hologram;
import com.golfing8.kcommon.hook.holograms.HologramProvider;
import eu.decentsoftware.holograms.api.DHAPI;
import org.bukkit.Location;

import java.util.UUID;

public class DHProvider implements HologramProvider {
    @Override
    public Hologram createHologram(Location location) {
        return new DHHologram(DHAPI.createHologram(UUID.randomUUID().toString(), location));
    }
}
