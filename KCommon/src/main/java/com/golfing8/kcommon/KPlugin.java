package com.golfing8.kcommon;

import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.golfing8.kcommon.hook.placeholderapi.KPAPIHook;
import com.golfing8.kcommon.library.LibraryLoader;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuManager;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import com.golfing8.kcommon.module.ModuleManifest;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.struct.KNamespacedKey;
import com.golfing8.kcommon.util.Reflection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

/**
 * An abstract plugin that uses this library.
 */
public abstract class KPlugin extends JavaPlugin implements LangConfigContainer {
    @Getter
    private MenuManager menuManager;
    @Getter
    private LangConfig langConfig;
    /**
     * The placeholderAPI hook for the entire plugin.
     */
    @Getter
    private KPAPIHook placeholderAPIHook;
    /**
     * The module manifest
     */
    @Getter
    private ModuleManifest manifest;
    /**
     * Dynamic library loader
     */
    protected LibraryLoader libraryLoader;

    @Override
    public final void onLoad() {
        this.libraryLoader = new LibraryLoader(this, getDataFolder().toPath().resolve("libraries"));
        this.onLoadInner();
    }

    /**
     * A final implementation for loading things related to the plugin
     */
    public final void onEnable() {
        try {
            this.saveDefaultConfig();
        } catch (IllegalArgumentException ignored) {
        } // Config doesn't exist
        this.menuManager = new MenuManager(this);
        //Setup PAPI.
        this.placeholderAPIHook = new KPAPIHook(this);
        this.placeholderAPIHook.register();

        // Run the pre-enable inner function
        this.onPreEnableInner();
        if (!getServer().getPluginManager().isPluginEnabled(this))
            return;

        //Set up the lang config.
        Path langPath = Paths.get(getDataFolder().getPath(), "kore-lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        this.loadLangConstants();
        this.loadModuleManifest();

        this.onEnableInner();
        if (!getServer().getPluginManager().isPluginEnabled(this))
            return;

        try {
            Reflection.reflectivelySetupModules((PluginClassLoader) getClassLoader(), module -> {
                this.placeholderAPIHook.registerProvider(module);
            });
        } catch (RuntimeException e) {
            this.getLogger().log(Level.SEVERE, "Failed to reflectively initialize modules! Shutting down...", e);
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Finally, save the lang config again in case anything was registered!
        this.langConfig.save();
    }

    /**
     * A final implementation for shutting down things in the plugin
     */
    public final void onDisable() {
        onDisableInner();

        for (Module module : Modules.getAll()) {
            if (module.getPlugin() != this)
                continue;

            try {
                module.shutdown();
            } catch (Throwable thr) {
                getLogger().log(Level.SEVERE, String.format("Experienced uncaught error while shutting down module %s!", module.getModuleName()), thr);
            }
        }

        //Shutdown all menus.
        for (Menu menu : this.menuManager.getAll()) {
            menu.shutdown();
        }

        this.placeholderAPIHook.unregister();

        //Once more save the lang config.
        if (this.langConfig != null) {
            this.langConfig.save();
        }
        saveModuleManifest();
    }

    @Override
    public File getFile() {
        return super.getFile();
    }

    /**
     * Loads language constants for the main language file.
     */
    protected void loadLangConstants() {
        this.langConfig.addLanguageConstant("kore-reload-command-reloaded", "&aReloaded the &e{MODULE} &amodule in &e{TIME}ms&a.");
        this.langConfig.addLanguageConstant("generic-command-not-player", "&cOnly players can do that!");
        this.langConfig.addLanguageConstant("invalid-argument", "&cArgument '{ARGUMENT}' at position {POSITION} is invalid! Was expecting a '{TYPE}', you entered {ACTUAL}!");
        this.langConfig.addLanguageConstant("missing-argument", "&cArgument '{ARGUMENT}' at position {POSITION} was missing! Was expecting a '{TYPE}'!");
        this.langConfig.addLanguageConstant("no-permission", "&cYou don't have permission to use this command!");
        this.langConfig.addLanguageConstant("command-help", Message.builder()
                .pageHeader("&6&m-----&r {PREVIOUS} &6Help for command: /{COMMAND} &e{PAGE}&7/&e{MAX_PAGE} {NEXT} &6&m-----")
                .messages(Lists.newArrayList("%COMMAND_HELP%"))
                .paged(true)
                .build());
        this.langConfig.addLanguageConstant("command-help-none-found", "&cNo command matches found :(");
        this.langConfig.addLanguageConstant("command-help-format", "&e/{COMMAND}&6{ARGUMENTS} &a{DESCRIPTION}");
    }

    private void saveModuleManifest() {
        if (this.manifest == null)
            return;

        try {
            BufferedWriter writer = Files.newBufferedWriter(getDataFolder().toPath().resolve("module-manifest.json"));
            DataSerializer.getGSONBase().toJson(this.manifest, writer);
            writer.close();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Failed to save module-manifest.json!", e);
        }
    }

    /**
     * Tries to load the module manifest.
     */
    private void loadModuleManifest() {
        Path path = getDataFolder().toPath().resolve("module-manifest.json");
        if (Files.notExists(path)) {
            // Load default manifest.
            this.manifest = new ModuleManifest();
            try {
                Files.createFile(path);
            } catch (IOException exc) {
                getLogger().log(Level.SEVERE, "Failed to save default module-manifest.json!", exc);
            }
            return;
        }

        Gson gsonBase = DataSerializer.getGSONBase();
        try {
            ModuleManifest loadedManifest = gsonBase.fromJson(Files.newBufferedReader(path), ModuleManifest.class);
            this.manifest = loadedManifest == null || loadedManifest.getModuleStates() == null ? new ModuleManifest() : loadedManifest;
        } catch (IOException exc) {
            getLogger().log(Level.SEVERE, "Failed to read module-manifest.json! Loading default manifest...", exc);
            this.manifest = new ModuleManifest();
        }
    }

    /**
     * A method stub for {@link #onLoad()} overrides
     */
    public void onLoadInner() {

    }

    /**
     * A method stub for an early {@link #onEnable()} override
     */
    public void onPreEnableInner() {

    }

    /**
     * A method stub for {@link #onEnable()} overrides
     */
    public void onEnableInner() {

    }

    /**
     * A method stub for {@link #onDisable()} override
     */
    public void onDisableInner() {

    }
}
