package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.config.lang.LangConfigEnum;
import com.golfing8.kcommon.config.yaml.YamlConfig;
import com.golfing8.kcommon.event.EcsEvents;
import com.golfing8.kcommon.hook.placeholderapi.KPAPIHook;
import com.golfing8.kcommon.hook.placeholderapi.KPlaceholderDefinition;
import com.golfing8.kcommon.hook.placeholderapi.PlaceholderProvider;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.event.IAsyncEvent;
import com.hypixel.hytale.event.IBaseEvent;
import com.hypixel.hytale.registry.Registration;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * A module on a Hytale server. All 'Features' should be implemented through an extension of this class.
 * <p>
 * Hytale has its own UI system, so unlike the Bukkit module there is no menu support.
 * Register commands with {@link #addCommand}, events with {@link #addListener} and ECS events with
 * {@link #addEcsListener} so they are removed when the module is disabled.
 * </p>
 */
public abstract class Module extends AbstractModule<YamlConfig> implements LangConfigContainer, PlaceholderProvider {
    /**
     * Gets a module instance associated with the given type for the call.
     *
     * @param unused an unused array of objects used to reify the generic.
     * @param <T>    the type.
     * @return the module instance.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Module> T get(@NotNull T... unused) {
        return (T) Modules.getModule((Class<? extends Module>) unused.getClass().getComponentType());
    }

    private final List<EventRegistration<?, ?>> eventRegistrations = new ArrayList<>();
    /**
     * The commands this module controls. They are unregistered when this module is disabled,
     * so the module should register them again in {@link #onEnable()}.
     */
    @Getter
    private final List<MCommand<?>> moduleCommands = new ArrayList<>();
    /**
     * The language config for this module.
     */
    @Getter
    private LangConfig langConfig;
    /**
     * Stores simple placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, PlaceholderFunction> placeholders = new TreeMap<>();
    /**
     * Stores relational placeholders registered to this module.
     */
    @Getter
    private final TreeMap<KPlaceholderDefinition, RelPlaceholderFunction> relationalPlaceholders = new TreeMap<>();

    public Module(KPlugin plugin, String moduleName, Set<String> moduleDependencies, Set<String> pluginDependencies) {
        super(plugin, moduleName, moduleDependencies, pluginDependencies);
    }

    public Module(KPlugin plugin, String moduleName) {
        this(plugin, moduleName, Collections.emptySet(), Collections.emptySet());
    }

    protected Module() {
        super();
    }

    @Override
    public KPlugin getPlugin() {
        return (KPlugin) super.getPlugin();
    }

    @Override
    protected ModuleManifest getModuleManifest() {
        return getPlugin().getModuleManifest();
    }

    @Override
    protected YamlConfig createConfig(Path configPath, @Nullable byte[] defaults) {
        YamlConfig config = new YamlConfig(configPath, this);
        if (defaults != null) {
            YamlConfig source = new YamlConfig();
            source.loadFromString(new String(defaults, StandardCharsets.UTF_8));
            config.setSource(source);
        }
        return config;
    }

    @Override
    protected void loadConfigFile(YamlConfig config) {
        config.load();
    }

    @Override
    protected void loadLanguage() {
        Path langPath = getPlugin().getPluginDataFolder().resolve(getModuleName()).resolve("lang.yml");
        this.langConfig = new LangConfig(langPath);
        this.langConfig.load();

        if (getModuleInfo() != null) {
            for (Class<?> source : getModuleInfo().langSources()) {
                this.loadLangEnum(source.asSubclass(LangConfigEnum.class));
            }
        }
        this.loadContainer();
    }

    @Override
    protected void registerPlatformHooks() {
        // Events are registered by the module itself through addListener.
    }

    @Override
    protected void afterEnable() {
        KPAPIHook hook = getPlugin().getPlaceholderAPIHook();
        if (hook != null)
            hook.registerProvider(this);

        this.langConfig.save();
    }

    @Override
    protected void beforeDisable() {
        this.langConfig.save();
    }

    @Override
    protected void unregisterPlatformHooks() {
        for (EventRegistration<?, ?> registration : eventRegistrations) {
            if (registration.isRegistered())
                registration.unregister();
        }
        eventRegistrations.clear();

        this.moduleCommands.forEach(MCommand::unregister);
        this.moduleCommands.clear();

        KPAPIHook hook = getPlugin().getPlaceholderAPIHook();
        if (hook != null)
            hook.unregisterProvider(this);

        this.relationalPlaceholders.clear();
        this.placeholders.clear();
    }

    /**
     * Adds a command to this module. It is unregistered when this module is disabled.
     *
     * @param mCommand the command.
     */
    protected final void addCommand(MCommand<?> mCommand) {
        this.moduleCommands.add(mCommand);
        mCommand.register();
    }

    /**
     * Listens to a Hytale event until this module is disabled.
     *
     * @param eventClass the event class.
     * @param handler    the handler.
     * @param <E>        the event type.
     */
    protected final <E extends IBaseEvent<Void>> void addListener(Class<? super E> eventClass, Consumer<E> handler) {
        eventRegistrations.add(getPlugin().getEventRegistry().register(eventClass, handler));
    }

    /**
     * Listens to an async Hytale event, such as {@code PlayerConnectEvent}, until this module is disabled.
     *
     * @param eventClass the event class.
     * @param handler    the handler, which may chain onto the future.
     * @param <E>        the event type.
     */
    protected final <E extends IAsyncEvent<Void>> void addAsyncListener(Class<? super E> eventClass, Function<CompletableFuture<E>, CompletableFuture<E>> handler) {
        eventRegistrations.add(getPlugin().getEventRegistry().registerAsync(eventClass, handler));
    }

    /**
     * Listens to an entity ECS event, such as {@code BreakBlockEvent} or {@code Damage}, until this module is disabled.
     *
     * @param eventClass the event class.
     * @param handler    the handler.
     * @param <E>        the event type.
     */
    protected final <E extends EcsEvent> void addEcsListener(Class<E> eventClass, EcsEvents.Handler<E> handler) {
        bind(EcsEvents.listen(eventClass, handler));
    }

    /**
     * Unregisters the given registration, such as a global event listener, when this module is disabled.
     *
     * @param registration the registration.
     */
    public final void addRegistration(Registration registration) {
        bind((AutoCloseable) () -> {
            if (!registration.isRegistered())
                return;

            try {
                registration.unregister();
            } catch (IllegalArgumentException exc) {
                // May occur if the registration thinks it is registered, but the registry doesn't find it.
                getLogger().log(Level.WARNING, "Unexpected error when unregistering registration for " + getModuleName(), exc);
            }
        });
    }

    /**
     * Registers an entity ECS system until this module is disabled.
     *
     * @param system the system.
     */
    @SuppressWarnings("unchecked")
    public final void registerSystem(ISystem<EntityStore> system) {
        getPlugin().getEntityStoreRegistry().registerSystem(system);
        bind((AutoCloseable) () -> {
            // The registry is already shut down when the server stops, and then removes the system itself.
            if (!EntityStore.REGISTRY.isShutdown() && EntityStore.REGISTRY.hasSystemClass(system.getClass()))
                EntityStore.REGISTRY.unregisterSystem((Class<? extends ISystem<EntityStore>>) system.getClass());
        });
    }

    /**
     * Repeats the task until this module is disabled. Delays and periods are in Minecraft ticks (50 ms).
     *
     * @param task        the task.
     * @param delayTicks  the delay before the first run.
     * @param periodTicks the time between runs.
     * @param async       if the task should run off the world thread.
     * @return the task handle.
     */
    protected final Terminable addTimer(Runnable task, long delayTicks, long periodTicks, boolean async) {
        return bind(async ? Platform.get().runTimerAsync(task, delayTicks, periodTicks) : Platform.get().runTimer(task, delayTicks, periodTicks));
    }

    @NotNull
    @Override
    public String getPlaceholderKey() {
        return this.getModuleName();
    }

    @Override
    public void addPlaceholder(KPlaceholderDefinition definition, PlaceholderFunction function) {
        this.placeholders.put(definition, function);
    }

    @Override
    public void addRelPlaceholder(KPlaceholderDefinition definition, RelPlaceholderFunction function) {
        this.relationalPlaceholders.put(definition, function);
    }

    /**
     * Checks if the player has the permission under this module's permission prefix.
     *
     * @param player     the player.
     * @param permission the permission, or empty for the prefix itself.
     * @return true if the player has the permission.
     */
    public boolean hasPermission(PlayerRef player, String permission) {
        return player.hasPermission(permission == null || permission.isEmpty() ? getPermissionPrefix() : getPermissionPrefix() + "." + permission);
    }
}
