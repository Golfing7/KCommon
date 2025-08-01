package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.config.commented.KConfigurationSection;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Acts as a parent class to all class style configs. Every non-transient field in a config class will
 * be saved and mirrored to a config.
 * <p>
 * As a side note, it is recommend that a singleton instance of the class is stored in the parent of this instance.
 * </p>
 */
public abstract class ConfigClass {
    /**
     * Maps a field to its handle.
     */
    private final Map<Field, ConfigValueHandle> fieldHandleMap = new HashMap<>();
    /**
     * The children of this config class, mapped to their singleton instance.
     */
    private final Map<Class<? extends ConfigClass>, ConfigClass> children = new HashMap<>();
    /**
     * The class backing this config.
     */
    private final Class<?> self;
    /**
     * All source classes to load
     */
    private final Set<ConfigClassSource> extraSources = Collections.newSetFromMap(new IdentityHashMap<>());
    /**
     * The backing instance of this config
     */
    @Getter
    private final Object configInstance;
    /**
     * The parent class of this config.
     */
    @Nullable
    @Getter
    private ConfigClass parent;
    /**
     * The optional conf annotation for this class
     */
    @Nullable
    @Getter
    private Conf confAnnotation;
    /**
     * If this config class requires the {@link Conf} annotation on fields.
     */
    @Getter
    @Setter
    private boolean requireAnnotation = false;
    /**
     * If this config class is in 'read only' mode. This will make it so default values are NOT set
     */
    @Getter
    @Setter
    private boolean readOnly;
    /**
     * If config mapping should be enabled for loading/saving values
     */
    @Getter
    @Setter
    private boolean configMappingEnabled = false;

    public ConfigClass() {
        this.parent = null;
        this.self = this.getClass();
        this.configInstance = this;
    }

    public ConfigClass(@Nullable ConfigClass parent) {
        this.parent = parent;
        this.self = this.getClass();
        this.configInstance = this;
    }

    protected ConfigClass(@Nullable ConfigClass parent, @Nonnull Class<?> delegate, @Nonnull Object configInstance) {
        this.parent = parent;
        this.self = delegate;
        this.configInstance = configInstance;
    }

    /**
     * Gets all expected config names from all present annotations.
     *
     * @return the expected config names.
     */
    public Set<String> getConfigNames() {
        Set<String> allNames = new HashSet<>();
        for (ConfigClass child : this.children.values()) {
            allNames.addAll(child.getConfigNames());
        }
        for (ConfigValueHandle handle : this.fieldHandleMap.values()) {
            if (handle.getAnnotation() == null)
                continue;

            if (handle.getAnnotation().config().equals(Conf.DEFAULT_CONF)) {
                allNames.add("config");
            } else {
                allNames.add(handle.getAnnotation().config().toLowerCase());
            }
        }
        return allNames;
    }

    /**
     * Gets a child of this config class.
     *
     * @param clazz the class.
     * @param <T>   the type of the child.
     * @return the child.
     */
    @SuppressWarnings("unchecked")
    public <T extends ConfigClass> T getChild(Class<T> clazz) {
        ConfigClass configClass = this.children.get(clazz);
        if (configClass == null)
            throw new NoSuchElementException(String.format("Config class %s is not a child of %s!", clazz.getName(), self.getName()));
        return (T) configClass;
    }

    /**
     * Unregisters this config class.
     */
    public void unregister() {
    }

    /**
     * Adds a source to this config class where values will be loaded from
     *
     * @param source the source
     */
    public final void addSource(Class<? extends ConfigClassSource> source) {
        ConfigClassSource instance;
        try {
            instance = source.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            throw new RuntimeException("Failed to instantiate config source from class " + source.getName(), e);
        }

        if (!this.extraSources.add(instance))
            return;

        this.resolveFields(instance.getClass(), instance);
    }

    /**
     * Sets up the structure of the config class.
     */
    public final void initConfig() {
        this.resolveChildren();
        this.resolveFields(this.self, this.configInstance);
        for (ConfigClassSource source : this.extraSources) {
            this.resolveFields(source.getClass(), source);
        }

        children.values().forEach(ConfigClass::initConfig);
    }

    /**
     * Loads and sets all values from the given config section.
     *
     * @param source the config source to load from.
     * @return true if the config section was modified, false if not
     */
    public final boolean loadValues(ConfigurationSection source) {
        String path = buildPath();
        boolean modified = false;
        if (source instanceof KConfigurationSection && this.confAnnotation != null) {
            ((KConfigurationSection) source).setComments(path, this.confAnnotation.value());
        }

        for (ConfigValueHandle handle : this.fieldHandleMap.values()) {
            String fieldPath = handle.getFormattedPath(path);
            modified |= handle.load(source, fieldPath, readOnly, configMappingEnabled);
        }

        for (ConfigClass child : children.values()) {
            modified |= child.loadValues(source);
        }
        return modified;
    }

    /**
     * Builds the path to the current config class instance.
     *
     * @return the built path.
     */
    protected String buildPath() {
        if (this.parent == null)
            return "";

        // If we have a parent, we need to somehow distinguish our path.
        String name = this.confAnnotation != null && !this.confAnnotation.label().isEmpty() ?
                this.confAnnotation.label() : this.self.getSimpleName();
        String parentPath = parent.buildPath();
        return !parentPath.isEmpty() ? parentPath + "." + name : name;
    }

    /**
     * Resolves the fields of this instance.
     */
    private void resolveFields(Class<?> clazz, Object instance) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if ((modifiers & (Modifier.TRANSIENT | Modifier.FINAL)) != 0)
                continue;

            // If we don't require the annotation, don't do anything w/ static fields.
            if (!requireAnnotation && (modifiers & (Modifier.STATIC)) != 0)
                continue;

            // Check for the required annotation.
            if (requireAnnotation && field.getAnnotation(Conf.class) == null)
                continue;

            field.setAccessible(true);

            //Generate and insert the field.
            FieldHandle<?> generatedHandle = FieldHandles.getHandle(field.getName(), clazz);
            this.fieldHandleMap.put(field, new ConfigValueHandle(generatedHandle, field.getAnnotation(Conf.class), instance));
        }

        Class<?> parent = clazz.getSuperclass();
        if (parent != ConfigClass.class && ConfigClass.class.isAssignableFrom(parent) && !children.containsKey(parent)) {
            resolveFields(parent, instance);
        }
    }

    /**
     * Finds and loads the children classes.
     */
    @SuppressWarnings("unchecked")
    private void resolveChildren() {
        Class<?>[] classes = self.getDeclaredClasses();
        for (Class<?> clazz : classes) {
            // Check if the class is an instance of a config class.
            if (!ConfigClass.class.isAssignableFrom(clazz))
                continue;

            try {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                ConfigClass instance = (ConfigClass) constructor.newInstance();
                instance.parent = this;
                if (clazz.isAnnotationPresent(Conf.class))
                    instance.confAnnotation = clazz.getAnnotation(Conf.class);

                this.children.put((Class<? extends ConfigClass>) clazz, instance);
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(String.format("Failed to load child config class %s.", clazz.getName()), e);
            }
        }
    }
}
