package com.golfing8.kcommon;

import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.hook.placeholderapi.KPAPIHook;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleLoader;
import com.golfing8.kcommon.module.ModuleManifest;
import com.golfing8.kcommon.module.Modules;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginBase;
import com.hypixel.hytale.server.core.plugin.PluginManager;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.Logger;

/**
 * An abstract Hytale plugin that uses this library.
 * <p>
 * Plugins extend this instead of {@link JavaPlugin}. Modules in the plugin's jar are found and enabled
 * during Hytale's {@code start} phase and shut down with the plugin.
 * </p>
 */
public abstract class KPlugin extends JavaPlugin implements KPluginContext, LangConfigContainer {
    private final Logger logger;
    @Getter
    private LangConfig langConfig;
    /**
     * The module manifest, storing which modules are enabled.
     */
    @Getter
    private ModuleManifest moduleManifest;
    /**
     * The PlaceholderAPI hook for this plugin, or null if PlaceholderAPI isn't installed.
     */
    @Getter
    private @Nullable KPAPIHook placeholderAPIHook;

    public KPlugin(JavaPluginInit init) {
        super(init);
        // Hytale doesn't print plain JUL records, so pass this logger's records on to the plugin's Hytale logger.
        this.logger = Logger.getLogger(getManifest().getName());
        this.logger.setUseParentHandlers(false);
        for (Handler handler : this.logger.getHandlers()) {
            this.logger.removeHandler(handler);
        }
        this.logger.addHandler(new Handler() {
            private final Formatter formatter = new SimpleFormatter();

            @Override
            public void publish(LogRecord record) {
                HytaleLogger.Api api = getLogger().at(record.getLevel());
                if (record.getThrown() != null)
                    api = api.withCause(record.getThrown());
                String prefix = record.getLoggerName() == null || record.getLoggerName().equals(logger.getName()) ? "" : "[" + record.getLoggerName() + "] ";
                api.log(prefix + formatter.formatMessage(record));
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
    }

    @Override
    protected final void setup() {
        onSetupInner();
    }

    @Override
    protected final void start() {
        this.langConfig = new LangConfig(getDataDirectory().resolve("kore-lang.yml"));
        this.langConfig.load();
        this.langConfig.addLanguageConstant("invalid-argument", "&cArgument '{ARGUMENT}' at position {POSITION} is invalid! Was expecting a '{TYPE}', you entered {ACTUAL}!");
        this.langConfig.addLanguageConstant("missing-argument", "&cArgument '{ARGUMENT}' at position {POSITION} was missing! Was expecting a '{TYPE}'!");
        this.langConfig.addLanguageConstant("no-permission", "&cYou don't have permission to use this command!");
        this.langConfig.addLanguageConstant("command-help", "&6&m-----&r &6Help for command: /{COMMAND} &6&m-----", "%COMMAND_HELP%");
        this.langConfig.addLanguageConstant("command-help-none-found", "&cNo command matches found :(");
        this.langConfig.addLanguageConstant("command-help-format", "&e/{COMMAND}&6{ARGUMENTS} &a{DESCRIPTION}");
        this.moduleManifest = ModuleManifest.load(getDataDirectory().resolve("module-manifest.json"), logger);

        if (isPluginEnabled("PlaceholderAPI")) {
            this.placeholderAPIHook = new KPAPIHook(this);
            this.placeholderAPIHook.register();
        }

        onEnableInner();

        ModuleLoader.setupModules(getClassLoader(), this, info -> {
            for (String depend : info.pluginDependencies()) {
                if (!isPluginEnabled(depend))
                    return false;
            }
            return true;
        }, module -> {
            if (placeholderAPIHook != null)
                placeholderAPIHook.registerProvider((Module) module);
        }, () -> logger.severe("Modules of " + getPluginName() + " depend on each other in a cycle, none were enabled!"));

        this.langConfig.save();
    }

    @Override
    protected final void shutdown() {
        onDisableInner();

        for (Module module : Modules.getAll()) {
            if (module.getPlugin() != this)
                continue;

            try {
                module.shutdown();
            } catch (Throwable thr) {
                logger.log(Level.SEVERE, String.format("Experienced uncaught error while shutting down module %s!", module.getModuleName()), thr);
            }
        }

        if (this.placeholderAPIHook != null)
            this.placeholderAPIHook.unregister();
        if (this.langConfig != null)
            this.langConfig.save();
        if (this.moduleManifest != null)
            this.moduleManifest.save(getDataDirectory().resolve("module-manifest.json"), logger);
    }

    /**
     * Checks if a plugin with the given name, or {@code Group:Name} identifier, is enabled.
     *
     * @param name the name.
     * @return true if it is enabled.
     */
    protected static boolean isPluginEnabled(String name) {
        for (PluginBase plugin : PluginManager.get().getPlugins()) {
            if ((plugin.getManifest().getName().equals(name) || plugin.getIdentifier().toString().equals(name)) && plugin.isEnabled())
                return true;
        }
        return false;
    }

    @Override
    public String getPluginName() {
        return getManifest().getName();
    }

    @Override
    public Logger getPluginLogger() {
        return logger;
    }

    @Override
    public Path getPluginDataFolder() {
        return getDataDirectory();
    }

    @Override
    public Path getPluginJar() {
        return getFile();
    }

    /**
     * Called during Hytale's setup phase, before any module loads. Register components and systems here.
     */
    public void onSetupInner() {
    }

    /**
     * Called during Hytale's start phase, right before this plugin's modules are enabled.
     */
    public void onEnableInner() {
    }

    /**
     * Called when the plugin shuts down, before its modules are disabled.
     */
    public void onDisableInner() {
    }
}
