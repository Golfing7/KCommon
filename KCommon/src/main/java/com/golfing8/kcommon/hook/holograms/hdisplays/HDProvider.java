package com.golfing8.kcommon.hook.holograms.hdisplays;

import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.hook.holograms.Hologram;
import com.golfing8.kcommon.hook.holograms.HologramProvider;
import org.bukkit.Location;

public class HDProvider implements HologramProvider {
    @Override
    public Hologram createHologram(Location location) {
        com.gmail.filoghost.holographicdisplays.api.Hologram hologram = HologramsAPI.createHologram(KCommon.getInstance(), location);
        return new HDHologram(hologram);
    }
}
