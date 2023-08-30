package com.golfing8.kcommon;

import com.golfing8.kcommon.nms.access.NMSAccess;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Controls the NMS access.
 */
public final class NMS {
    /**
     * The loaded NMS access, resolved from the {@link #initialize()} method.
     */
    @Getter
    private static NMSAccess theNMS;

    /**
     * Loads the NMS from the bukkit version.
     */
    public static void initialize(){
        if(theNMS != null)
            throw new IllegalStateException("NMS is already initialized!");

        String theVersion = version();

        String vSplit = theVersion.split("R")[0];

        try{
            Class<?> mainClass = Class.forName("com.golfing8.kcommon.nms." + vSplit.substring(0, vSplit.length() - 1) + ".NMS");

            Constructor<?> cons = mainClass.getConstructor(Plugin.class);

            theNMS = (NMSAccess) cons.newInstance(KCommon.getInstance());
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException ignored) {

        }
    }

    /**
     * Gets the string bukkit version.
     *
     * @return the bukkit version.
     */
    private static String version(){
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
