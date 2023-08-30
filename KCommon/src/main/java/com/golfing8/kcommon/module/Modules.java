package com.golfing8.kcommon.module;

import com.golfing8.kcommon.KPlugin;
import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage the instances of the {@link Module} class. This is used in place of the
 * main {@link KPlugin} class to ensure that other plugins can register modules.
 * (Potential future functionality)
 */
@UtilityClass
public final class Modules {
    /**
     * Maps modules from their name to their instance.
     * <p />
     * Note that module names are stored as lowercase strings to ensure that no two modules can share
     * similar names.
     */
    private static final Map<String, Module> MODULE_MAP = new HashMap<>();
    /**
     * Stores the module's class to its instance.
     */
    private static final Map<Class<? extends Module>, Module> CLASS_MODULE_MAP = new HashMap<>();

    /**
     * Gets all modules registered.
     *
     * @return the collection of all modules registered.
     */
    public static Collection<Module> getAll() {
        return new ArrayList<>(MODULE_MAP.values());
    }

    /**
     * Checks if the given name of a module is registered.
     *
     * @param moduleName the name of the module
     * @return true if the module exists
     */
    public static boolean moduleExists(@Nonnull String moduleName) {
        Preconditions.checkNotNull(moduleName);
        return MODULE_MAP.containsKey(moduleName.toLowerCase());
    }

    /**
     * Gets the module with the given name, or null if the module does not exist.
     *
     * @param moduleName the module's name.
     * @return the module, or null if no such module exists.
     */
    @Nullable
    public static Module getModule(@Nonnull String moduleName) {
        Preconditions.checkNotNull(moduleName);
        return MODULE_MAP.get(moduleName.toLowerCase());
    }

    /**
     * Gets the given module with the class type.
     *
     * @param moduleClass the class of the module to get
     * @param <T> the type of module
     */
    @SuppressWarnings("unchecked")
    public static <T extends Module> T getModule(@Nonnull Class<T> moduleClass) {
        Preconditions.checkNotNull(moduleClass);
        return (T) CLASS_MODULE_MAP.get(moduleClass);
    }

    /**
     * A method for the {@link Module} class to call when it needs to register itself.
     * This method should not be called by any other class.
     *
     * @param module the module to register.
     * @return true if the module registered successfully, false if it already exists/was registered
     */
    static boolean registerModule(@Nonnull Module module) {
        Preconditions.checkNotNull(module);
        if(MODULE_MAP.containsKey(module.getModuleName().toLowerCase()))
            return false;

        MODULE_MAP.put(module.getModuleName().toLowerCase(), module);
        CLASS_MODULE_MAP.put(module.getClass(), module);
        return true;
    }

    /**
     * Unregisters the given module from the module map. Note that the module should still call its own
     * shutdown logic as this method will not.
     *
     * @param module the module to unregister.
     */
    static void unregisterModule(@Nonnull Module module) {
        Preconditions.checkNotNull(module);
        MODULE_MAP.remove(module.getModuleName());
        CLASS_MODULE_MAP.remove(module.getClass());
    }
}
