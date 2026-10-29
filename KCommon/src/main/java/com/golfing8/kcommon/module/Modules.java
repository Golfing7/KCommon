package com.golfing8.kcommon.module;

import com.golfing8.kcommon.struct.KNamespacedKey;
import lombok.experimental.UtilityClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Typed access to the {@link ModuleRegistry} for Bukkit modules.
 */
@UtilityClass
public final class Modules {
    /**
     * Gets all modules currently registered.
     *
     * @return all modules.
     */
    public static Collection<Module> getAll() {
        List<Module> modules = new ArrayList<>();
        for (AbstractModule<?> module : ModuleRegistry.getAll()) {
            modules.add((Module) module);
        }
        return modules;
    }

    /**
     * Checks if a module exists with the given name.
     *
     * @param moduleName the name of the module.
     * @return true if the module exists.
     */
    public static boolean moduleExists(@Nonnull String moduleName) {
        return ModuleRegistry.moduleExists(moduleName);
    }

    /**
     * Checks if a module exists with the given key.
     *
     * @param namespacedKey the key of the module.
     * @return true if the module exists.
     */
    public static boolean moduleExists(@Nonnull KNamespacedKey namespacedKey) {
        return ModuleRegistry.moduleExists(namespacedKey);
    }

    /**
     * Gets the module with the given name.
     *
     * @param moduleName the name of the module.
     * @return the module, or null if none exists.
     */
    @Nullable
    public static Module getModule(@Nonnull String moduleName) {
        return (Module) ModuleRegistry.getModule(moduleName);
    }

    /**
     * Gets the module with the given key.
     *
     * @param namespacedKey the key of the module.
     * @return the module, or null if none exists.
     */
    @Nullable
    public static Module getModule(@Nonnull KNamespacedKey namespacedKey) {
        return (Module) ModuleRegistry.getModule(namespacedKey);
    }

    /**
     * Gets the module instance of the given class.
     *
     * @param moduleClass the class of the module.
     * @param <T>         the type of the module.
     * @return the module, or null if none exists.
     */
    public static <T extends Module> T getModule(@Nonnull Class<T> moduleClass) {
        return ModuleRegistry.getModule(moduleClass);
    }

    static boolean registerModule(@Nonnull Module module) {
        return ModuleRegistry.registerModule(module);
    }

    static void unregisterModule(@Nonnull Module module) {
        ModuleRegistry.unregisterModule(module);
    }
}
