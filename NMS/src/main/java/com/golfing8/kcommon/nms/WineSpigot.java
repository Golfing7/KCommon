package com.golfing8.kcommon.nms;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;

@UtilityClass
public class WineSpigot {
    private static Boolean isWineSpigot;

    public static boolean isWineSpigot(){
        if(isWineSpigot == null)
            return isWineSpigot = Bukkit.getServer().getName().equals("WineSpigot");
        return isWineSpigot;
    }
}
