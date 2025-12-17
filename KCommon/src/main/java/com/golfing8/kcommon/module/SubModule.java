package com.golfing8.kcommon.module;

import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.lang.LangConf;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.util.StringUtil;
import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import java.util.logging.Level;

/**
 * Submodules act as 'partner' classes to their parent modules. These classes CAN be singleton.
 * <p>
 * The purpose of submodules are to provide the ability to split a module class into submodules based
 * upon responsibilities, rather than the typical design of 'put all listeners in the module class'.
 * </p>
 * <p>
 * Sub modules are NOT registered reflectively and MUST be registered manually.
 * </p>
 * <p>
 * {@link Conf} and {@link LangConf} annotations still work as expected.
 * </p>
 * <p>
 * Note that the {@link #module} reference is may be null until {@link #onEnable()} is called.
 * </p>
 *
 * @param <T>
 */
public abstract class SubModule<T extends Module> extends ConfigClass implements Listener, LangConfigContainer {
    @Getter
    private T module;

    public SubModule() {
        this.setRequireAnnotation(true);
        this.setConfigMappingEnabled(true);
        this.initConfig();
    }

    void link(T module) {
        this.module = module;
    }

    @Override
    public final void unregister() {
        super.unregister();

        HandlerList.unregisterAll(this);
        try {
            onDisable();
        } catch (Throwable thr) {
            module.getLogger().log(Level.SEVERE, String.format("Failed to disable sub-module %s!", getClass().getSimpleName()), thr);
        }
    }

    @Override
    public String getPrefix() {
        return "sub." + StringUtil.camelToYaml(getClass().getSimpleName());
    }

    @Override
    public LangConfig getLangConfig() {
        return module.getLangConfig(); // Piggy back :)
    }

    /**
     * Empty stub for onEnable implementation
     */
    public void onEnable() {
    }

    /**
     * Empty stub for onDisable implementation
     */
    public void onDisable() {
    }
}
