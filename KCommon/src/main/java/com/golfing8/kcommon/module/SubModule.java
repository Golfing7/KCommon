package com.golfing8.kcommon.module;

import com.golfing8.kcommon.command.exc.CommandInstantiationException;
import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.config.generator.ConfigClass;
import com.golfing8.kcommon.config.generator.ConfigClassWrapper;
import com.golfing8.kcommon.config.lang.LangConf;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.util.Reflection;
import com.golfing8.kcommon.util.StringUtil;
import org.bukkit.event.Listener;

import java.util.List;

/**
 * Submodules act as 'partner' classes to their parent modules.
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
 * @param <T>
 */
public abstract class SubModule<T extends Module> extends ConfigClass implements Listener, LangConfigContainer {
    public final T module;
    @SuppressWarnings("unchecked")
    public SubModule() {
        List<Class<?>> classes = Reflection.getSuperParameterizedTypes(this.getClass());
        if (classes.size() != 1)
            throw new RuntimeException("Cannot reflectively find module class!");

        Module m = Modules.getModule((Class<? extends Module>) classes.get(0));
        if (m == null)
            throw new CommandInstantiationException(String.format("Cannot reflectively find module class! Given class was: %s", classes.get(0)));

        this.module = (T) m;

        this.setRequireAnnotation(true);
        this.initConfig();
    }

    @Override
    public String getPrefix() {
        return "sub." + StringUtil.camelToYaml(getClass().getSimpleName());
    }

    @Override
    public LangConfig getLangConfig() {
        return module.getLangConfig(); // Piggy back :)
    }

    public void onEnable() {}

    public void onDisable() {}
}
