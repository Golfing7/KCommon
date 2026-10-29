package com.golfing8.kcommon;

import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * What shared KCommon code needs to know about the plugin that owns a module or data manager.
 * <p>
 * The method names deliberately differ from Bukkit's and Hytale's plugin methods so each platform's
 * {@code KPlugin} can implement this without clashing with its server's plugin base class.
 * </p>
 */
public interface KPluginContext {
    /**
     * Gets the plugin's name.
     *
     * @return the name.
     */
    String getPluginName();

    /**
     * Gets the plugin's logger.
     *
     * @return the logger.
     */
    Logger getPluginLogger();

    /**
     * Gets the folder the plugin stores its files in.
     *
     * @return the data folder.
     */
    Path getPluginDataFolder();

    /**
     * Gets the plugin's jar file.
     *
     * @return the jar file.
     */
    Path getPluginJar();
}
