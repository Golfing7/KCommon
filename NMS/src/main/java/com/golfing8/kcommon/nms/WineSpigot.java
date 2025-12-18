package com.golfing8.kcommon.nms;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

/**
 * Utility class for checking if the server is WineSpigot
 */
@UtilityClass
public class WineSpigot {
    private static Boolean isWineSpigot;

    /**
     * Checks if the server is winespigot
     *
     * @return true if winespigot
     */
    public static boolean isWineSpigot() {
        if (isWineSpigot == null)
            return isWineSpigot = Bukkit.getServer().getName().equals("WineSpigot");
        return isWineSpigot;
    }
}
