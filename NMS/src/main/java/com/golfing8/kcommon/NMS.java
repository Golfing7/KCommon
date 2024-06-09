package com.golfing8.kcommon;

import com.golfing8.kcommon.nms.access.NMSAccess;
import lombok.Getter;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.TreeMap;

/**
 * Controls the NMS access.
 */
public final class NMS {
    /**
     * Maps new nms versions to their package names.
     */
    private static final TreeMap<NMSVersion, String> newVersionToPackageNumberMapping = new TreeMap<NMSVersion, String>() {
        {
            put(new NMSVersion(8, 0), "v1_8");

            put(new NMSVersion(12, 0), "v1_12");

            put(new NMSVersion(16, 0), "v1_16");

            put(new NMSVersion(17, 0), "v1_17");

            put(new NMSVersion(18, 0), "v1_18");

            put(new NMSVersion(19, 0), "v1_19");
            put(new NMSVersion(19, 2), "v1_19_R2");
            put(new NMSVersion(19, 4), "v1_19_R3");

            put(new NMSVersion(20, 0), "v1_20");
            put(new NMSVersion(20, 1), "v1_20_R2");
            put(new NMSVersion(20, 3), "v1_20_R3");
            put(new NMSVersion(20, 5), "v1_20_R4");
        }
    };
    @Getter
    private static NMSAccess theNMS;
    @Getter
    private static NMSVersion serverVersion;

    public static void initialize(Plugin plugin) {
        if (theNMS != null)
            throw new IllegalStateException("NMS is already initialized!");

        serverVersion = NMSVersion.loadVersion();
        var packageName = newVersionToPackageNumberMapping.floorEntry(serverVersion);
        if (packageName == null)
            throw new IllegalArgumentException(String.format("Unrecognized NMS version %s!", serverVersion.toString()));

        try {
            Class<?> mainClass = Class.forName("com.golfing8.kcommon.nms." + packageName.getValue() + ".NMS");

            Constructor<?> cons = mainClass.getConstructor(Plugin.class);

            theNMS = (NMSAccess) cons.newInstance(plugin);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                 InstantiationException e) {
            try {
                Class<?> mainClass = Class.forName("com.golfing8.kcommon.nms.unknown.NMS");

                Constructor<?> cons = mainClass.getConstructor(Plugin.class);

                theNMS = (NMSAccess) cons.newInstance(plugin);
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException |
                     InstantiationException e2) {
                e2.printStackTrace();
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }
}
