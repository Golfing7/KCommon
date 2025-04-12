package com.golfing8.kcommon.command;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.KPlugin;
import com.golfing8.kcommon.command.argument.ArgumentContext;
import com.golfing8.kcommon.command.argument.CommandArgument;
import com.golfing8.kcommon.command.exc.CommandInstantiationException;
import com.golfing8.kcommon.command.requirement.Requirement;
import com.golfing8.kcommon.command.requirement.RequirementPlayer;
import com.golfing8.kcommon.config.lang.LangConfig;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * An abstract KCommon command. Can be registered without a module and should be used for plugin-wide commands.
 */
public abstract class KCommand implements TabExecutor {
    private static final Pattern HELP_PATTERN = Pattern.compile(
            "(help|\\?)",
            Pattern.CASE_INSENSITIVE
    );

    @Data
    @AllArgsConstructor
    @RequiredArgsConstructor
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
        private @Nullable Function<CommandSender, Object> autoComplete = null;
        /**
         * If players do not have this required permission extension, they will not be able
         * to override the auto complete function.
         * <p>
         * Note that in a chain of arguments, if a player doesn't have permission for one argument,
         * the remaining arguments will also be autofilled.
         * </p>
         */
        @Setter
        private @Nullable String requiredPermissionExtension;
        /**
         * true = Only players are allowed to use the autofill for this argument.
         * false = Only console is allowed to autofill this argument.
         * null = Anyone can autofill this argument.
         */
        private @Nullable Boolean autoFillPlayersOnly;

        public BuiltCommandArgument(String name, CommandArgument<?> argument, @Nullable Function<CommandSender, Object> autoComplete) {
            this.name = name;
            this.argument = argument;
            this.autoComplete = autoComplete;
        }
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
     * The plugin that registered this command.
     */
    @Getter
    private final Plugin plugin;
    /**
     * The requirements for executing this command.
     */
    @Getter
    private final Set<Requirement> commandRequirements = new HashSet<>();
    /** The source that this command uses for its lang */
    @Getter
    private final LangConfig langSource;
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
    @Getter @Setter
    private String description;
    /**
     * The parent of this command, can be null.
     */
    @Getter @Nullable
    private KCommand parent;

    /** If this command should be run async */
    @Getter @Setter
    private boolean async;

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
        this.plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        this.commandPermission = cmd.permission();
        this.commandName = cmd.name();
        this.commandAliases = Arrays.asList(cmd.aliases());
        this.description = cmd.description();
        this.visibility = cmd.visibility();
        this.async = cmd.async();
        if (cmd.forPlayers()) {
            this.commandRequirements.add(RequirementPlayer.getInstance());
        }
        if (plugin instanceof KPlugin) {
            this.langSource = ((KPlugin) plugin).getLangConfig();
        } else {
            this.langSource = KCommon.getInstance().getLangConfig();
        }
    }

    public KCommand(String commandName, List<String> commandAliases, boolean forPlayers) {
        this.commandName = commandName;
        this.commandAliases = commandAliases;
        this.plugin = JavaPlugin.getProvidingPlugin(this.getClass());
        if (forPlayers) {
            this.commandRequirements.add(RequirementPlayer.getInstance());
        }
        if (plugin instanceof KPlugin) {
            this.langSource = ((KPlugin) plugin).getLangConfig();
        } else {
            this.langSource = KCommon.getInstance().getLangConfig();
        }
    }

    /**
     * Checks if this command can only be executed by players.
     *
     * @return true if only players can execute this command.
     */
    public boolean isOnlyForPlayers() {
        return this.commandRequirements.contains(RequirementPlayer.getInstance());
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

    protected String getGeneratedCommandPermission() {
        String prefixInsertion = getCommandPermissionPrefix();
        String builtPrefix;
        if (StringUtil.isNotEmpty(prefixInsertion)) {
            builtPrefix = JavaPlugin.getProvidingPlugin(this.getClass()).getName().toLowerCase() + "." + prefixInsertion + ".command.";
        } else {
            builtPrefix = JavaPlugin.getProvidingPlugin(this.getClass()).getName().toLowerCase() + ".command.";
        }
        return builtPrefix + buildCommandPermissionSuffix();
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

        this.commandPermission = getGeneratedCommandPermission();
    }

    /**
     * Recursive method for calling sub commands' onRegister methods.
     */
    protected final void subRegister() {
        for(KCommand sub : this.subcommands) {
            try {
                sub.onRegister();
                sub.subRegister();
            } catch (Exception exc) {
                throw new RuntimeException("Failed to initialize subcommand " + sub.getCommandName() + "!", exc);
            }
        }

        this.setupPermission();
        this.internalOnRegister();
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
        CommandManager.getInstance().registerNewCommand(plugin, this, true);
        this.onRegister();
        this.subRegister();
    }

    /**
     * Unregisters this command from the bukkit command map.
     */
    public final void unregister() {
        CommandManager.getInstance().unregisterCommand(this);
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
        this.subcommands.sort(Comparator.comparing(KCommand::getCommandName));
    }

    /**
     * Adds an argument to this command.
     *
     * @param name the name of the argument.
     * @param argument the argument to add.
     */
    protected final BuiltCommandArgument addArgument(String name, CommandArgument<?> argument) {
        BuiltCommandArgument arg = new BuiltCommandArgument(name, argument);
        this.commandArguments.add(arg);
        return arg;
    }

    /**
     * Adds the argument and its autofill function to the argument list.
     *
     * @param name the name of the argument.
     * @param argument the argument to add.
     * @param autofill the autofill function.
     */
    protected final BuiltCommandArgument addArgument(String name, CommandArgument<?> argument, Function<CommandSender, Object> autofill) {
        BuiltCommandArgument arg = new BuiltCommandArgument(name, argument, autofill);
        this.commandArguments.add(arg);
        return arg;
    }

    /**
     * Adds the given command requirement to this command.
     *
     * @param requirement the requirement.
     */
    protected final void addRequirement(Requirement requirement) {
        this.commandRequirements.add(requirement);
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
            handleHelpMessage(sender, null);
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
            if (!canSeeArgument(sender, builtCommandArgument)) {
                if (builtCommandArgument.getAutoFillPlayersOnly() != null) {
                    boolean player = sender instanceof Player;
                    if (player != builtCommandArgument.getAutoFillPlayersOnly()) {
                        handleMissingArgument(sender, builtCommandArgument);
                        continue;
                    }
                }

                builtArguments.add(Objects.toString(builtCommandArgument.autoComplete.apply(sender), null));
                // If we fail the 'can see' check once, make sure all remaining arguments are auto completed.
                break;
            }
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
                    handleHelpMessage(sender, null);
                }
                return null;
            }

            if (builtCommandArgument.getAutoFillPlayersOnly() != null) {
                boolean player = sender instanceof Player;
                if (player != builtCommandArgument.getAutoFillPlayersOnly()) {
                    handleMissingArgument(sender, builtCommandArgument);
                    continue;
                }
            }

            //Create the argument context and test it.
            builtArguments.add(Objects.toString(autofill.apply(sender), null));
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
     * Checks that the context meets all requirements.
     *
     * @param context the context.
     * @return if they meet the requirements.
     */
    private boolean checkRequirements(CommandContext context) {
        for (Requirement requirement : this.commandRequirements) {
            if (!requirement.meetsRequirement(context)) {
                requirement.getErrorMessage(context).send(context.getSender());
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the sender has permission to execute this command.
     *
     * @param sender the sender.
     * @return if they have permission.
     */
    public boolean checkPermission(CommandSender sender) {
        return StringUtil.isEmpty(this.commandPermission) || sender.hasPermission(this.commandPermission);
    }

    /**
     * Checks if the given sender has the given extension of the command's permission.
     *
     * @param sender the sender.
     * @param extension the extension.
     * @return if they have the permission extension.
     */
    public boolean checkPermissionExtension(CommandSender sender, String extension) {
        if (StringUtil.isEmpty(this.commandPermission)) // If the command has no permission, there's nothing we can do here.
            throw new IllegalStateException("Cannot check permission extension with null permission.");

        return sender.hasPermission(this.commandPermission + "." + extension);
    }

    /**
     * Checks if the player can see the given command argument.
     * <p>
     * In particular, this checks if the player has the {@link BuiltCommandArgument#requiredPermissionExtension} permission
     * and that the command argument is an autocomplete argument.
     * </p>
     *
     * @param sender the command sender.
     * @param argument the argument.
     * @return true if they can see the argument.
     */
    public boolean canSeeArgument(CommandSender sender, BuiltCommandArgument argument) {
        if (argument.autoComplete == null)
            return true;

        if (argument.requiredPermissionExtension == null)
            return true;

        return checkPermissionExtension(sender, argument.requiredPermissionExtension);
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
            // As soon as a player cannot see a command argument, prevent adding the rest.
            if (!canSeeArgument(sender, argument))
                break;

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

        String builtArguments = this.commandArguments.isEmpty() ? "" : " " + argumentChain.toString().trim();
        String message = langSource.getMessage("command-help-format").getMessages().get(0);
        return MS.parseSingle(message,
                "COMMAND", commandChain.toString().trim(),
                "ARGUMENTS", builtArguments,
                "DESCRIPTION", getDescription() == null ? "No description" : getDescription());
    }

    /**
     * Handles a user that is requesting a help message for this command.
     *
     * @param sender the sender.
     * @param lastArgument the last argument provided in the command.
     */
    private void handleHelpMessage(CommandSender sender, String lastArgument) {
        if (!canSee(sender)) {
            return;
        }

        Message message = langSource.getMessage("command-help");
        List<String> commandHelp = new ArrayList<>();
        String help = getDescriptiveCommandHelp(sender);
        if (help != null)
            commandHelp.add(help);
        commandHelp.addAll(getHelpMessages0(sender, lastArgument != null ? lastArgument.toLowerCase() : null));
        if (commandHelp.isEmpty()) {
            commandHelp.add(langSource.getMessage("command-help-none-found").getMessages().get(0));
        }
        message.send(
                sender,
                Placeholder.compileCurly("COMMAND", this.getFullCommandChain()),
                Collections.singleton(MultiLinePlaceholder.percent("COMMAND_HELP", commandHelp))
        );
    }

    /**
     * Sends sub command help.
     *
     * @param sender the sender.
     */
    private List<String> getHelpMessages0(CommandSender sender, @Nullable String lastArgument) {
        List<String> allMessages = new ArrayList<>();
        for (KCommand sub : this.subcommands) {
            if (!matchesForHelpMessage(lastArgument, sub.getCommandName()))
                continue;

            if (!sub.canSee(sender))
                continue;

            String result = sub.getDescriptiveCommandHelp(sender);
            if (result != null)
                allMessages.add(result);
            allMessages.addAll(sub.getHelpMessages0(sender, lastArgument));
        }
        return allMessages;
    }

    /**
     * Checks if the argument matches the command name close enough for it to be displayed in the help message.
     *
     * @param argument the argument.
     * @param commandName the command name.
     * @return true if the command matches.
     */
    private static boolean matchesForHelpMessage(@Nullable String argument, String commandName) {
        if (argument == null)
            return true;

        return commandName.startsWith(argument) ||
                // Levenshtein is calculated with a greater cost for replace/swap as we *really* only want to match prefixes/suffixes.
                StringUtil.levenshteinDistance(argument, commandName, 1, 1, 2, 2) < argument.length() * 1.5;
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

        langSource.getMessage("invalid-argument").send(sender, "POSITION", this.commandArguments.indexOf(argument), "ARGUMENT", argument.getName(), "TYPE", argument.getArgument().getDescription(), "ACTUAL", actual);
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

        langSource.getMessage("missing-argument").send(sender, "POSITION", this.commandArguments.indexOf(argument), "ARGUMENT", argument.getName(), "TYPE", argument.getArgument().getDescription());
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
            langSource.getMessage("no-permission").send(sender);
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
        if (builtContext == null) {
            return;
        }

        if (!this.checkRequirements(builtContext)) {
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
        if (async) {
            Bukkit.getScheduler().runTaskAsynchronously(this.getPlugin(), () -> this.pass(commandSender, s, strings));
        } else {
            this.pass(commandSender, s, strings);
        }
        return true;
    }

    @Override
    public final List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if(strings.length == 0)
            return Collections.emptyList();

        return gatherTabCompletions(commandSender, s, strings);
    }

    /**
     * Used to register internals for {@link MCommand}.
     */
    void internalOnRegister() {}

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
    protected void execute(@NotNull CommandContext context) {
        // Send a help message.
        this.handleHelpMessage(context.getSender(), context.getArguments().isEmpty() ? null : context.getArguments().get(0));
    }
}
