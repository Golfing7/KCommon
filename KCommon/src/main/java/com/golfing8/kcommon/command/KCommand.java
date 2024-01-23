package com.golfing8.kcommon.command;

import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.argument.ArgumentContext;
import com.golfing8.kcommon.command.argument.CommandArgument;
import com.golfing8.kcommon.command.exc.CommandInstantiationException;
import com.golfing8.kcommon.util.MS;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.spigotmc.SpigotConfig;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An abstract KCommon command. Can be registered without a module and should be used for plugin-wide commands.
 */
@RequiredArgsConstructor
public abstract class KCommand implements TabExecutor {
    private static final Pattern HELP_PATTERN = Pattern.compile(
            "(help|\\?)",
            Pattern.CASE_INSENSITIVE
    );

    @RequiredArgsConstructor
    @AllArgsConstructor
    @Data
    public static final class BuiltCommandArgument {
        /**
         * The name of the argument.
         */
        private final String name;
        /**
         * The command argument backing this built command argument.
         */
        private final CommandArgument<?> argument;
        /**
         * The auto-complete function. Can be null.
         */
        private Function<CommandSender, Object> autoComplete = null;
    }

    /**
     * The name of this command.
     */
    @Getter
    private final String commandName;
    /**
     * This command's aliases.
     */
    @Getter
    private final List<String> commandAliases;
    /**
     * If this command should only be runnable by players.
     */
    @Getter
    private final boolean onlyForPlayers;
    /**
     * The arguments for this command, kept with a nullable autofill function.
     */
    @Getter
    private final List<BuiltCommandArgument> commandArguments = new ArrayList<>();
    /**
     * The sub commands of this command.
     */
    @Getter
    private final List<KCommand> subcommands = new ArrayList<>();
    /**
     * The annotation defining the structure of this command.
     */
    @Getter
    private Cmd annotation;
    /** The visibility of the command */
    @Getter
    private CommandVisibility visibility = CommandVisibility.PUBLIC;
    /**
     * The permission required to execute this command.
     * <p>
     * This is set after {@link #onRegister()} is called as it can be dynamically built.
     * </p>
     */
    @Getter @Setter
    private String commandPermission = "";
    /** A description of this command */
    @Getter
    private String description;
    /**
     * The parent of this command, can be null.
     */
    @Getter @Nullable
    private KCommand parent;

    /** The last time this command was executed. */
    @Getter
    private long lastExecutionTime;

    /**
     * A constructor to initialize all fields with the {}
     */
    public KCommand() {
        Cmd cmd = getClass().getAnnotation(Cmd.class);
        if(cmd == null)
            throw new CommandInstantiationException(String.format("Cannot instantiate command '%s' with default constructor without Cmd annotation!", this.getClass().getName()));

        this.annotation = cmd;
        this.commandPermission = cmd.permission();
        this.commandName = cmd.name();
        this.commandAliases = Arrays.asList(cmd.aliases());
        this.onlyForPlayers = cmd.forPlayers();
        this.description = cmd.description();
        this.visibility = cmd.visibility();
    }

    /**
     * If execution has been implemented on this command.
     *
     * @return the command.
     */
    public boolean isExecutionImplemented() {
        try {
            this.getClass().getDeclaredMethod("execute", CommandContext.class);
            return true;
        } catch (NoSuchMethodException exc) {
            return false;
        }
    }

    /**
     * Sets the parent of this command, can be null.
     *
     * @param parent the parent.
     */
    protected void setParent(@Nullable KCommand parent) {
        this.parent = parent;
    }

    /**
     * Builds the suffix of the command permission.
     * The suffix is the part that comes after the <code>{PLUGIN_NAME}.command</code> prefix.
     */
    protected String buildCommandPermissionSuffix() {
        List<String> parentNames = Lists.newArrayList(this.commandName);
        KCommand parent = this.parent;
        while (parent != null) {
            parentNames.add(parent.commandName);
            parent = parent.getParent();
        }
        Collections.reverse(parentNames);

        return String.join(".", parentNames);
    }

    /**
     * Gets the prefix to this command's permission.
     * <p>
     * This is the part of the permission that's inserted BEFORE 'command'.
     * If null/empty, nothing is inserted.
     * </p>
     * <p>
     * e.g. If this method returns {@code "lifesteal"}, then the permission's prefix transforms from
     * <br>
     * {@code PLUGIN.command.COMMAND_NAME}
     * <br>
     * to
     * <br>
     * {@code PLUGIN.lifesteal.command.COMMAND_NAME}
     * </p>
     *
     * @return the prefix.
     */
    protected String getCommandPermissionPrefix() {
        return null;
    }

    /**
     * Sets up the permission for using this command.
     */
    private void setupPermission() {
        // If the annotation is null, we have no business in setting this back up.
        if (this.annotation == null)
            return;

        // Is it not dynamic? Then there's no reason to change it.
        if (!this.annotation.permission().equals(Cmd.GENERATE_PERMISSION))
            return;

        String prefixInsertion = getCommandPermissionPrefix();
        String builtPrefix;
        if (StringUtil.isNotEmpty(prefixInsertion)) {
            builtPrefix = KPlugin.getProvidingPlugin(this.getClass()).getName().toLowerCase() + "." + prefixInsertion + ".command.";
        } else {
            builtPrefix = KPlugin.getProvidingPlugin(this.getClass()).getName().toLowerCase() + ".command.";
        }
        this.commandPermission = builtPrefix + buildCommandPermissionSuffix();
    }

    /**
     * Recursive method for calling sub commands' onRegister methods.
     */
    protected final void subRegister() {
        for(KCommand sub : this.subcommands) {
            sub.onRegister();
            sub.subRegister();
        }

        this.setupPermission();
    }

    /**
     * Recursive method for calling sub commands' onUnregister methods.
     */
    protected final void subUnregister() {
        for(KCommand sub : this.subcommands) {
            sub.onUnregister();
            sub.subUnregister();
        }
    }

    /**
     * Registers this command to the bukkit command map.
     */
    public final void register() {
        ((KPlugin) KPlugin.getProvidingPlugin(this.getClass())).getCommandManager().registerNewCommand(this);
        this.onRegister();
        this.subRegister();
    }

    /**
     * Unregisters this command from the bukkit command map.
     */
    public final void unregister() {
        ((KPlugin) KPlugin.getProvidingPlugin(this.getClass())).getCommandManager().unregisterCommand(this);
        this.onUnregister();
        this.subUnregister();
    }

    /**
     * Adds a subcommand to the list of subcommands this command manages.
     *
     * @param command the subcommand.
     */
    protected final void addSubCommand(KCommand command) {
        if(command == this)
            throw new CommandInstantiationException("Cannot add self as sub command!");

        if(this.subcommands.contains(command))
            throw new CommandInstantiationException("That subcommand is already registered!");

        this.subcommands.add(command);
        command.setParent(this);
    }

    /**
     * Adds an argument to this command.
     *
     * @param name the name of the argument.
     * @param argument the argument to add.
     */
    protected final void addArgument(String name, CommandArgument<?> argument) {
        this.commandArguments.add(new BuiltCommandArgument(name, argument));
    }

    /**
     * Adds the argument and its autofill function to the argument list.
     *
     * @param name the name of the argument.
     * @param argument the argument to add.
     * @param autofill the autofill function.
     */
    protected final void addArgument(String name, CommandArgument<?> argument, Function<CommandSender, Object> autofill) {
        this.commandArguments.add(new BuiltCommandArgument(name, argument, autofill));
    }

    /**
     * Builds the command context from the given sender, label, and arguments.
     * If the arguments are not valid, null is returned.
     *
     * @param sender the sender of the command.
     * @param label the label of the command used.
     * @param args the arguments used in the command.
     * @param verbose if something goes wrong, should we send a message?
     * @return the command context or null.
     */
    @Nullable
    private CommandContext buildContext(CommandSender sender, String label, String[] args, boolean verbose) {
        if (args.length > 0 && HELP_PATTERN.matcher(args[0]).matches()) {
            handleHelpMessage(sender, true);
            return null;
        }

        List<String> builtArguments = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            String stringArgument = args[i];

            //Check if we should just immediately add the argument.
            if(this.commandArguments.size() <= i) {
                builtArguments.add(stringArgument);
                continue;
            }

            BuiltCommandArgument builtCommandArgument = this.commandArguments.get(i);
            CommandArgument<?> commandArgument = builtCommandArgument.getArgument();

            //Create the argument context and test it.
            ArgumentContext context = new ArgumentContext(sender, this, label, stringArgument);
            if(!commandArgument.getPredicate().test(context)) {
                // Handle the argument as invalid.
                if (verbose) {
                    handleInvalidArgument(sender, builtCommandArgument, stringArgument);
                }
                return null;
            }
            builtArguments.add(stringArgument);
        }

        //Try to fill in the 'autofill' arguments.
        for (int i = builtArguments.size(); i < this.commandArguments.size(); i++) {
            BuiltCommandArgument builtCommandArgument = this.commandArguments.get(i);
            Function<CommandSender, Object> autofill = builtCommandArgument.getAutoComplete();
            if(autofill == null) {
                // Handle the argument as missing.
                if (verbose) {
                    handleMissingArgument(sender, builtCommandArgument);

                    // Check if the help command should be sent.
                    handleHelpMessage(sender, i == 0);
                }
                return null;
            }

            //Create the argument context and test it.
            builtArguments.add(autofill.apply(sender).toString());
        }
        return new CommandContext(sender, label, builtArguments, this);
    }

    /**
     * Checks for sub commands and runs them if found.
     *
     * @param sender the command sender.
     * @param args the arguments of the command.
     * @return true if a sub command was found and run, false if not.
     */
    private boolean checkSubcommands(CommandSender sender, String[] args) {
        if(this.subcommands.isEmpty() || args.length == 0)
            return false;

        //Build the new parts of the command.
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        String newLabel = args[0];

        //Check all subcommands.
        for(KCommand subcommand : this.subcommands) {
            if(subcommand.labelMatches(newLabel)) {
                subcommand.pass(sender, newLabel, newArgs);
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the sender has permission to execute this command.
     *
     * @param sender the sender.
     * @return if they have permission.
     */
    public boolean checkPermission(CommandSender sender) {
        return this.commandPermission.isEmpty() || sender.hasPermission(this.commandPermission);
    }

    /**
     * Checks if the given sender has the given extension of the command's permission.
     *
     * @param sender the sender.
     * @param extension the extension.
     * @return if they have the permission extension.
     */
    public boolean checkPermissionExtension(CommandSender sender, String extension) {
        if (this.commandPermission.isEmpty()) // If the command has no permission, there's nothing we can do here.
            return true; // TODO Make *every* command have a permission

        return sender.hasPermission(this.commandPermission + "." + extension);
    }

    /**
     * Checks if the user can see this command.
     *
     * @param sender the sender.
     * @return if they can see this command.
     */
    private boolean canSee(CommandSender sender) {
        if (visibility == CommandVisibility.PRIVATE)
            return false;

        return visibility == CommandVisibility.PUBLIC || (this.checkPermission(sender) && visibility == CommandVisibility.PROTECTED);
    }

    /**
     * Gets this command as a full chain.
     *
     * @return the full command chain.
     */
    public String getFullCommandChain() {
        StringBuilder commandChain = new StringBuilder();
        KCommand current = this;
        while (current != null) {
            commandChain.insert(0, " ").insert(0, current.commandName);
            current = current.getParent();
        }
        return commandChain.toString().trim();
    }

    /**
     * Gets a descriptive message for this command.
     *
     * @return the descriptive message.
     */
    private String getDescriptiveCommandHelp(CommandSender sender) {
        if (!isExecutionImplemented())
            return null;

        StringBuilder commandChain = new StringBuilder();
        KCommand current = this;
        while (current != null) {
            commandChain.insert(0, " ").insert(0, current.commandName);
            current = current.getParent();
        }

        boolean consoleSender = sender instanceof ConsoleCommandSender;
        StringBuilder argumentChain = new StringBuilder();
        for (BuiltCommandArgument argument : this.commandArguments) {
            // Required or not?
            if (argument.getAutoComplete() == null) {
                argumentChain.append("(").append(argument.getName()).append(") ");
            } else {
                if (this.isOnlyForPlayers() && consoleSender) {
                    argumentChain.append("[").append(argument.getName()).append("] ");
                } else {
                    argumentChain.append("[").append(argument.getName()).append("=").append(argument.getAutoComplete().apply(sender)).append("] ");
                }
            }
        }

        String builtArguments = this.commandArguments.isEmpty() ? "" : " &6" + argumentChain.toString().trim();
        return MS.parseSingle((consoleSender ? "&c" : "&e") + "/" + commandChain.toString().trim() + builtArguments + " &a" + this.description);
    }

    /**
     * Handles a user that is requesting a help message for this command.
     *
     * @param sender the sender.
     * @param recursive if a help message for sub commands should be printed.
     */
    private void handleHelpMessage(CommandSender sender, boolean recursive) {
        if (!canSee(sender)) {
            return;
        }

        MS.pass(sender, "&e----- &6Help for command: /{COMMAND} &e-----", "COMMAND", this.getFullCommandChain());
        MS.pass(sender, getDescriptiveCommandHelp(sender));
        // Recursively handle sub commands.
        if (recursive) {
            handleHelpMessage0(sender);
        }
    }

    /**
     * Sends sub command help.
     *
     * @param sender the sender.
     */
    private void handleHelpMessage0(CommandSender sender) {
        for (KCommand sub : this.subcommands) {
            MS.pass(sender, sub.getDescriptiveCommandHelp(sender));
            sub.handleHelpMessage0(sender);
        }
    }

    /**
     * Handles a user that has entered an invalid argument.
     *
     * @param sender the sender.
     * @param argument the argument.
     * @param actual the actual argument.
     */
    private void handleInvalidArgument(CommandSender sender, BuiltCommandArgument argument, String actual) {
        if (!canSee(sender)) {
            return;
        }

        MS.pass(sender, "&cArgument '{ARGUMENT}' at position {POSITION} is invalid! Was expecting a '{TYPE}', you entered {ACTUAL}!",
                "POSITION", this.commandArguments.indexOf(argument), "ARGUMENT", argument.getName(), "TYPE", argument.getArgument().getDescription(), "ACTUAL", actual);
    }

    /**
     * Handles a user that has entered a missing argument.
     *
     * @param sender the sender.
     * @param argument the argument.
     */
    private void handleMissingArgument(CommandSender sender, BuiltCommandArgument argument) {
        if (!canSee(sender)) {
            return;
        }

        MS.pass(sender, "&cArgument '{ARGUMENT}' at position {POSITION} was missing! Was expecting a '{TYPE}'!",
                "POSITION", this.commandArguments.indexOf(argument), "ARGUMENT", argument.getName(), "TYPE", argument.getArgument().getDescription());
    }

    /**
     * Handles a user that is trying to execute this command but doesn't have permission.
     *
     * @param sender the sender.
     */
    private void handleNoPerms(CommandSender sender) {
        if (this.visibility == CommandVisibility.PRIVATE || this.visibility == CommandVisibility.PROTECTED) {
            MS.pass(sender, SpigotConfig.unknownCommandMessage);
        } else {
            MS.pass(sender, "&cYou don't have permission to use this command!");
        }
    }

    /**
     * A method to take the raw data from a bukkit command and run it through this one.
     *
     * @param sender the sender of the command.
     * @param label the label of the command used.
     * @param args the arguments used in the command.
     */
    public final void pass(CommandSender sender, String label, String[] args) {
        if (checkSubcommands(sender, args)) {
            return;
        }

        if (!checkPermission(sender)) {
            handleNoPerms(sender);
            return;
        }

        //Check for the command context.
        CommandContext builtContext = buildContext(sender, label, args, true);
        if(builtContext == null) {
            return;
        }

        this.lastExecutionTime = System.currentTimeMillis();
        this.execute(builtContext);
    }

    /**
     * Checks if the given label is the command name or an alias of this command.
     *
     * @param label the label to check.
     * @return true if the label matches.
     */
    public final boolean labelMatches(String label) {
        if(label.equalsIgnoreCase(this.commandName))
            return true;

        for(String alias : this.commandAliases) {
            if(label.equalsIgnoreCase(alias))
                return true;
        }
        return false;
    }

    /**
     * Checks if the label of this command starts with the given prefix.
     *
     * @param prefix the prefix of the command.
     * @return true if it starts with the prefix.
     */
    public final boolean labelStartsWith(String prefix) {
        String prefixToLower = prefix.toLowerCase();
        //Check main label.
        if(this.commandName.toLowerCase().startsWith(prefixToLower))
            return true;

        //Check all aliases.
        for(String alias : this.commandAliases) {
            if(alias.toLowerCase().startsWith(prefixToLower))
                return true;
        }
        return false;
    }

    /**
     * Gathers the tab completions for this command.
     *
     * @param sender the sender.
     * @param label the command's label used
     * @param args the arguments.
     * @return the list of tab completions.
     */
    public final List<String> gatherTabCompletions(CommandSender sender, String label, String[] args) {
        if(args.length == 0)
            return Collections.emptyList();

        //We need to check our subcommands.
        for (KCommand command : this.getSubcommands()) {
            if (!command.labelMatches(args[0]))
                continue;

            String[] newArguments = new String[args.length - 1];
            System.arraycopy(args, 1, newArguments, 0, newArguments.length);
            return command.gatherTabCompletions(sender, args[0], newArguments);
        }

        //Loop over the arguments to match.
        if(args.length > this.commandArguments.size() + this.subcommands.size())
            return Collections.emptyList();

        //Get the raw completions.
        String stringArgument = args[args.length - 1];
        List<String> completions = new ArrayList<>();
        if(this.commandArguments.size() > args.length - 1) {
            CommandArgument<?> argument = this.commandArguments.get(args.length - 1).getArgument();
            ArgumentContext context = new ArgumentContext(sender, this, label, stringArgument);
            completions.addAll(argument.getCompletions().apply(context));
        }

        //Then go through the subcommands and try to add those as well.
        String argToLower = stringArgument.toLowerCase();
        for(KCommand command : this.getSubcommands()) {
            //Add the main label.
            if(command.getCommandName().toLowerCase().startsWith(argToLower))
                completions.add(command.getCommandName());

            //Add all aliases.
            for(String alias : command.getCommandAliases()) {
                if(alias.toLowerCase().startsWith(argToLower))
                    completions.add(alias);
            }
        }

        //Finally, filter out the rest of the completions.
        return completions.stream().filter(string -> string.toLowerCase(Locale.ROOT).startsWith(stringArgument.toLowerCase())).collect(Collectors.toList());
    }

    @Override
    public final boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        //Immediately pass to the real handler.
        this.pass(commandSender, s, strings);
        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0)
            return Collections.emptyList();

        return gatherTabCompletions(commandSender, s, strings);
    }

    /**
     * Run when this command is registered. Should be used for bootstrapping things like command arguments.
     */
    protected void onRegister() {/*Intentionally empty*/}

    /**
     * Run when this command is unregistered. Can be overridden if necessary.
     */
    protected void onUnregister() {/*Intentionally empty*/}

    /**
     * Run when a player has executed this command and it needs to function.
     */
    protected void execute(CommandContext context) {
        // Send a help message.
        this.handleHelpMessage(context.getSender(), context.getArguments().size() == 0);
    }
}
