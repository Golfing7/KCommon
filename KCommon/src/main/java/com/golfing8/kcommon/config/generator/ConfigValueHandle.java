package com.golfing8.kcommon.config.generator;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.commented.Config;
import com.golfing8.kcommon.util.StringUtil;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.Objects;

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
     * @return true if the config was modified, false if not
     */
    public boolean load(ConfigurationSection sourceSection, String path, boolean readOnly) {
        if (!sourceSection.contains(path)) {
            if (readOnly)
                return false;

            if (annotation != null && sourceSection instanceof Config) {
                ((Config) sourceSection).set(path, handle.get(instance), this.annotation.value());
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
}
