package com.golfing8.kcommon.command;

import com.golfing8.kcommon.command.exc.CommandInstantiationException;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.LangConfigContainer;
import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.Modules;
import com.golfing8.kcommon.util.Reflection;
import lombok.Getter;

import java.util.List;

/**
 * An abstract module command, used in modules that need to register commands for themselves.
 */
public abstract class MCommand<T extends Module> extends KCommand implements LangConfigContainer {
    /**
     * The module this command is tied to.
     */
    @Getter
    private final T module;
    public MCommand(T module, String commandName, List<String> commandAliases, boolean forPlayers) {
        super(commandName, commandAliases, forPlayers);

        this.module = module;
    }

    public MCommand(T module) {
        super();
        this.module = module;
    }

    @SuppressWarnings("unchecked")
    public MCommand() {
        super();
        // Find the module reflectively.
        List<Class<?>> classes = Reflection.getSuperParameterizedTypes(this.getClass());
        if (classes.size() != 1)
            throw new CommandInstantiationException("Cannot reflectively find module class!");

        Module m = Modules.getModule((Class<? extends Module>) classes.get(0));
        if (m == null)
            throw new CommandInstantiationException(String.format("Cannot reflectively find module class! Given class was: %s", classes.get(0)));

        this.module = (T) m;
    }

    @Override
    final void internalOnRegister() {
        if (this.supportsLangEnums()) {
            this.loadLangEnums();
            this.getLangConfig().save();
        }
    }

    @Override
    protected String getCommandPermissionPrefix() {
        return this.module.getModuleName();
    }

    @Override
    public String getPrefix() {
        StringBuilder builder = new StringBuilder();

        //Generate the command prefix.
        KCommand parent = getParent();
        while(parent != null) {
            builder.append(parent.getCommandName()).append("-");
            parent = parent.getParent();
        }
        builder.append(this.getCommandName());
        builder.insert(0, "command.");

        return builder.toString();
    }

    @Override
    public LangConfig getLangConfig() {
        return getModule().getLangConfig();
    }
}
