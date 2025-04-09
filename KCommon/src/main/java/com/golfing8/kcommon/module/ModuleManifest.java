package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * A manifest for containing module information for an instance of {@link KPlugin}.
 * <p>
 * This primarily contains information on whether a module is enabled or not.
 * </p>
 */
public class ModuleManifest {
    /** The states of all modules controlled by the plugin */
    @Getter
    private Map<String, Boolean> moduleStates = new HashMap<>();

    /**
     * Called when a module is registering itself into the {@link Modules} class.
     *
     * @param module the module.
     * @return if the module is marked for enabling/disabling.
     */
    public boolean loadModule(Module module) {
        if (!moduleStates.containsKey(module.getModuleName())) {
            moduleStates.put(module.getModuleName(), true);
            return true;
        }
        return moduleStates.get(module.getModuleName());
    }

    /**
     * Sets the active status of the given module
     *
     * @param module the module.
     * @param active if the module is active.
     */
    public void setActive(Module module, boolean active) {
        if (!moduleStates.containsKey(module.getModuleName()))
            throw new IllegalArgumentException(String.format("Module %s not registered to this manifest.", module.getModuleName()));

        moduleStates.put(module.getModuleName(), active);
    }

    /**
     * Checks if the given module is active.
     *
     * @param module te module.
     * @return if its active
     */
    public boolean isActive(Module module) {
        if (!moduleStates.containsKey(module.getModuleName()))
            throw new IllegalArgumentException(String.format("Module %s not registered to this manifest.", module.getModuleName()));

        return moduleStates.get(module.getModuleName());
    }
}
