package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.commented.KConfigurationSection;
import com.golfing8.kcommon.config.commented.MConfiguration;
import com.golfing8.kcommon.util.StringUtil;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;

/**
 * Acts as a handle to a configuration value.
 */
@Getter
@AllArgsConstructor
public class ConfigValueHandle {
    /**
     * The handle to the config value.
     */
    private final FieldHandle<?> handle;
    /**
     * The annotation instance used to describe config options.
     */
    @Nullable
    private final Conf annotation;
    /**
     * The instance of the config class this field handle belongs to.
     */
    private final Object instance;

    /**
     * Loads the handle's value from the config or sets it.
     *
     * @param sourceSection the source section.
     * @param path the path to the value.
     * @param readOnly if we should only read in values and not write.
     * @param mappingEnabled if we should pay attention to the MConfiguration's file name.
     * @return true if the config was modified, false if not
     */
    public boolean load(ConfigurationSection sourceSection, String path, boolean readOnly, boolean mappingEnabled) {
        // If we're loading from an actual config, we should check that the section is a match.
        if (sourceSection.getRoot() instanceof MConfiguration && mappingEnabled) {
            if (!mapsTo((MConfiguration) sourceSection.getRoot()))
                return false;
        }
        try {
            if (!sourceSection.contains(path)) {
                if (readOnly)
                    return false;

                if (annotation != null && sourceSection instanceof KConfigurationSection) {
                    ((KConfigurationSection) sourceSection).set(path, handle.get(instance), this.annotation.value());
                } else {
                    ConfigPrimitive adapted = ConfigTypeRegistry.toPrimitive(handle.get(instance));
                    sourceSection.set(path, adapted.unwrap());
                }
                return true;
            } else {
                Object fromType = ConfigTypeRegistry.getFromType(new ConfigEntry(sourceSection, path), handle.getField());
                handle.set(instance, fromType);
                return false;
            }
        } catch (Exception exc) {
            throw new RuntimeException(String.format("Failed to load config value at path %s", path), exc);        }
    }

    /**
     * Gets the formatted path to this value.
     *
     * @param parent the parent.
     * @return the built path.
     */
    public String getFormattedPath(String parent) {
        String label = annotation == null || annotation.label().isEmpty() ? StringUtil.camelToYaml(handle.getField().getName()) : annotation.label();
        if (parent.isEmpty())
            return label;
        else
            return parent + "." + label;
    }

    /**
     * Gets the object from the field.
     *
     * @return the object.
     */
    public Object get() {
        return handle.get(instance);
    }

    /**
     * Sets the value in the field.
     *
     * @param obj the obj.
     */
    public void set(Object obj) {
        handle.set(instance, obj);
    }

    /**
     * Checks the that this value handle maps to the given configuration.
     *
     * @param configuration the configuration.
     * @return if this value handle maps to that config.
     */
    private boolean mapsTo(MConfiguration configuration) {
        String fileName = configuration.getFileNameNoExtension();
        if ((this.annotation == null || this.annotation.config().equals(Conf.DEFAULT_CONF)) && fileName.equals("config"))
            return true;

        if (this.annotation == null)
            return false;

        return this.annotation.config().toLowerCase().equals(fileName);
    }
}
