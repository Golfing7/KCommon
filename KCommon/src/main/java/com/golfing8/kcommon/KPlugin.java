package com.golfing8.kcommon;

import com.golfing8.kcommon.command.CommandManager;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
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
import com.google.gson.Gson;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * An abstract plugin that uses this library.
 */
public abstract class KPlugin extends JavaPlugin implements LangConfigContainer {
    @Getter
    private CommandManager commandManager;
    @Getter
    private MenuManager menuManager;
    @Getter
    private LangConfig langConfig;
    /**
     * The placeholderAPI hook for the entire plugin.
     */
    @Getter
    private KPAPIHook placeholderAPIHook;
    /** The module manifest */
    @Getter
    private ModuleManifest manifest;
    /** Dynamic library loader */
    protected LibraryLoader libraryLoader;

    public final void onEnable() {
        try {
            this.saveDefaultConfig();
        } catch (IllegalArgumentException ignored) {} // Config doesn't exist
        this.libraryLoader = new LibraryLoader(this, getDataFolder().toPath().resolve("libraries"));
        this.commandManager = new CommandManager(this);
        this.menuManager = new MenuManager(this);
        //Setup PAPI.
        this.placeholderAPIHook = new KPAPIHook(this);
        this.placeholderAPIHook.register();

        // Run the pre-enable inner function
        this.onPreEnableInner();

        //Set up the lang config.
        Path langPath = Paths.get(getDataFolder().getPath(), "kore-lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        this.loadLangConstants();
        this.langConfig.save();
        this.loadModuleManifest();

        this.onEnableInner();
        try {
            this.reflectivelySetupModules();
        } catch (RuntimeException e) {
            this.getLogger().warning("Failed to reflectively initialize modules! Shutting down...");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
        }
    }

    public final void onDisable() {
        onDisableInner();

        for (Module module : Modules.getAll()) {
            if (module.getPlugin() != this)
                continue;

            try {
                module.shutdown();
            } catch (Throwable thr) {
                getLogger().severe(String.format("Experienced uncaught error while shutting down module %s!", module.getModuleName()));
                thr.printStackTrace();
            }
        }

        //Shutdown all menus.
        for (Menu menu : this.menuManager.getAll()) {
            menu.shutdown();
        }

        this.placeholderAPIHook.unregister();

        //Once more save the lang config.
        this.langConfig.save();
        saveModuleManifest();
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
    }

    private void saveModuleManifest() {
        try {
            BufferedWriter writer = Files.newBufferedWriter(getDataFolder().toPath().resolve("module-manifest.json"));
            DataSerializer.getGSONBase().toJson(this.manifest, writer);
            writer.close();
        } catch (IOException e) {
            getLogger().warning("Failed to save module-manifest.json!");
            e.printStackTrace();
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
                getLogger().warning("Failed to save default module-manifest.json!");
                exc.printStackTrace();
            }
            return;
        }

        Gson gsonBase = DataSerializer.getGSONBase();
        try {
            ModuleManifest loadedManifest = gsonBase.fromJson(Files.newBufferedReader(path), ModuleManifest.class);
            this.manifest = loadedManifest == null || loadedManifest.getModuleStates() == null ? new ModuleManifest() : loadedManifest;
        } catch (IOException exc) {
            getLogger().warning("Failed to read module-manifest.json! Loading default manifest...");
            exc.printStackTrace();
            this.manifest = new ModuleManifest();
        }
    }

    /**
     * Uses reflection to detect all present module classes and instantiate them.
     */
    private void reflectivelySetupModules() {
        //A map storing module classes to their dependencies in graph like formation
        Map<Class<?>, List<Class<?>>> classToClassDependencyGraph = new HashMap<>();
        Map<Class<?>, Module> instances = new HashMap<>();
        BiMap<String, Class<?>> nameModuleMap = HashBiMap.create();

        Reflection.discoverModules((PluginClassLoader) getClassLoader()).forEach(mClass -> {
            //We only want to work on our own modules, not other plugins
            if (!mClass.getPackage().getName().startsWith(this.getClass().getPackage().getName()))
                return;

            if (!mClass.isAnnotationPresent(ModuleInfo.class))
                return;

            //Instantiate the module with the given information
            ModuleInfo info = mClass.getAnnotation(ModuleInfo.class);

            // We can filter modules that are missing plugin dependencies at this point.
            for (String depend : info.pluginDependencies()) {
                if (!Bukkit.getPluginManager().isPluginEnabled(depend)) {
                    return;
                }
            }

            Module instance;
            // Is the module already registered?
            // If so, don't re-register it!
            // This can happen with Kotlin 'object' declarations.
            KNamespacedKey namespace = new KNamespacedKey(this, info.name());
            if ((instance = Modules.getModule(namespace)) == null) {
                try {
                    Constructor<? extends Module> constructor = mClass.getConstructor();
                    instance = constructor.newInstance();
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), e);
                }
            }

            instances.put(mClass, instance);
            nameModuleMap.put(info.name(), mClass);
        });

        // Build the dependency graph and filter classes that are missing module dependencies
        instances.entrySet().removeIf(entry -> {
            List<Class<?>> classDepends = new ArrayList<>();
            for (String mdepend : entry.getValue().getModuleDependencies()) {
                if (!nameModuleMap.containsKey(mdepend) && !Modules.moduleExists(mdepend)) {
                    nameModuleMap.remove(entry.getValue().getModuleName());
                    return true;
                }

                classDepends.add(nameModuleMap.containsKey(mdepend) ? nameModuleMap.get(mdepend) : Modules.getModule(mdepend).getClass());
            }
            classToClassDependencyGraph.put(entry.getKey(), classDepends);
            return false;
        });

        //Loop through to detect cycles.
        Set<Class<?>> traversed = new HashSet<>();
        Queue<Class<?>> nextUp = new ArrayDeque<>();
        for (Map.Entry<Class<?>, List<Class<?>>> entry : classToClassDependencyGraph.entrySet()) {
            traversed.clear();
            nextUp.add(entry.getKey());
            traversed.add(entry.getKey());

            //Do a breadth first walk to properly detect cycles.
            while(!nextUp.isEmpty()) {
                Class<?> type = nextUp.poll();

                for(Class<?> value : classToClassDependencyGraph.get(type)) {
                    //If this is the case, we've detected a cycle.
                    if(!traversed.add(value)) {
                        getLogger().severe(String.format("Detected cycle in dependencies for module '%s'!", entry.getKey().getSimpleName()));
                        getServer().getPluginManager().disablePlugin(this);
                        return;
                    }

                    nextUp.add(value);
                }
            }
        }

        //Do a depth first walk to enable all modules, at this point we know there's no cycles.
        Set<Class<?>> enabled = new HashSet<>();
        Stack<Class<?>> dependencies = new Stack<>();
        for (Map.Entry<Class<?>, List<Class<?>>> entry : classToClassDependencyGraph.entrySet()) {
            if(enabled.contains(entry.getKey()))
                continue;

            dependencies.addAll(entry.getValue());

            //Enable all dependencies.
            while (!dependencies.isEmpty()) {
                //Get the dependency.
                Class<?> currDepend = dependencies.peek();
                List<Class<?>> depends = classToClassDependencyGraph.get(currDepend);

                //Does it have any dependencies? If so, enable them!
                if(!dependencies.isEmpty() && !enabled.containsAll(depends)) {
                    dependencies.addAll(depends);
                    continue;
                }

                //No depends! Just enable it now.
                dependencies.pop();

                //If it's already been enabled, simply skip it.
                if(!enabled.add(currDepend))
                    continue;

                instances.get(currDepend).initialize();
            }

            enabled.add(entry.getKey());
            Module mInstance = instances.get(entry.getKey());
            mInstance.initialize();

            this.placeholderAPIHook.registerProvider(mInstance);
        }
    }

    public void onPreEnableInner() {}
    public void onEnableInner() {}
    public void onDisableInner() {}
}
