package com.golfing8.kcommon;

import com.golfing8.kcommon.command.CommandManager;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.hook.placeholderapi.PKorePAPIHook;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuManager;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.util.Reflection;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.PluginClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    private PKorePAPIHook placeholderAPIHook;

    public final void onEnable() {
        this.commandManager = new CommandManager(this);
        this.menuManager = new MenuManager(this);
        //Setup PAPI.
        this.placeholderAPIHook = new PKorePAPIHook(this);
        this.placeholderAPIHook.register();

        //Set up the lang config.
        Path langPath = Paths.get(getDataFolder().getPath(), "kore-lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();
        this.loadLangConstants();
        this.langConfig.save();

        this.onEnableInner();
        try {
            this.reflectivelySetupModules();
        } catch (RuntimeException e) {
            this.getLogger().warning("Failed to reflectively initialize modules! Shutting down...");
            e.printStackTrace();
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }
    }

    public final void onDisable() {
        onDisableInner();

        for (Module module : Modules.getAll()) {
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
    }

    /**
     * Loads language constants for the main language file.
     */
    protected void loadLangConstants() {
        this.langConfig.addLanguageConstant("kore-reload-command-reloaded", "&aReloaded the &e{MODULE} &amodule in &e{TIME}ms&a.");
        this.langConfig.addLanguageConstant("generic-command-not-player", "&cOnly players can do that!");
    }

    /**
     * Uses reflection to detect all present module classes in the <code>com.golfing8.kore.module.all</code> package
     * and instantiates them.
     */
    private void reflectivelySetupModules() {
        Class<Module> moduleClass = Module.class;

        //A map storing module classes to their dependencies in graph like formation
        Map<Class<?>, List<Class<?>>> classToClassDependencyGraph = new HashMap<>();
        Map<Class<?>, Module> instances = new HashMap<>();

        Reflection.discoverModules((PluginClassLoader) getClassLoader()).forEach(mClass -> {
            //We only want to work on our own modules, not other plugins
            Bukkit.getLogger().info(mClass.getPackage().getName());
            Bukkit.getLogger().info(this.getClass().getPackage().getName());
            if (!mClass.getPackage().getName().startsWith(this.getClass().getPackage().getName()))
                return;

            if (Modules.getModule(mClass) != null)
                return;

            if (!mClass.isAnnotationPresent(ModuleInfo.class))
                return;

            //Instantiate the module with the given information
            ModuleInfo info = mClass.getAnnotation(ModuleInfo.class);
            Module instance;
            try {
                Constructor<? extends Module> constructor = mClass.getConstructor();
                instance = constructor.newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                     InvocationTargetException e) {
                throw new RuntimeException(String.format("Failed to instantiate module %s!", info.name()), e);
            }
            instances.put(mClass, instance);
            classToClassDependencyGraph.put(mClass, Arrays.asList(info.moduleDependencies()));
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

    public void onEnableInner() {}
    public void onDisableInner() {}
}
