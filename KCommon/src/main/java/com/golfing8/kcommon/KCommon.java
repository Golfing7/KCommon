package com.golfing8.kcommon;

import com.golfing8.kcommon.util.NMSVersion;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * A plugin implementation of {@link KPlugin} so this commons library can be loaded as a standalone.
 */
public class KCommon extends KPlugin{
    @Getter
    private static KCommon instance;
    /**
     * The time zone for this plugin to run in
     */
    @Getter
    private ZoneId timeZone;
    @Getter
    private boolean debug;
    /**
     * The version this server is running.
     */
    @Getter
    private NMSVersion serverVersion;
    @Override
    public void onEnableInner() {
        instance = this;
        try {
            this.timeZone = ZoneId.of(getConfig().getString("time-zone", "America/New_York"));
        } catch (DateTimeException exc) {
            getLogger().warning(String.format("Failed to load time zone %s. Defaulting to America/New_York", getConfig().getString("time-zone")));
            this.timeZone = ZoneId.of("America/New_York");
        }
        this.debug = getConfig().getBoolean("debug", false);
        this.serverVersion = NMSVersion.fromBukkitPackageName(Bukkit.getServer().getClass().getName().split("\\.")[3]);
        NMS.initialize();
    }
}
