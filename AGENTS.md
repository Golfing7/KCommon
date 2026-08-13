# AGENTS.md
This file acts as a top level description of how KCommon works.

## Project Management
KCommon strictly requires Java 21 and Java 8 to build.
* Building `./gradlew jar`
* Testing `No provided JUnit tests`

## Structure
Each module acts as the center of some feature and should be implemented as follows.
```java
@ModuleInfo(
        name = "somemodule",
        configSources = SomeModuleConf.class,
        langSources = SomeModuleLang.class
)
public class SomeModule extends Module {
    @Override
    public void onEnable() {
        // Called when the module is enabled.
        // This occurs when the server loads or the module is enabled via an administrator command '/km enable somemodule'
        // Also occurs when a module is reloaded via `/km reload somemodule'
    }

    @Override
    public void onDisable() {
        // Called when the module is disabled.
        // This occurs when the server loads or the module is enabled via an administrator command '/km disable somemodule'
        // Also occurs when a module is reloaded via `/km reload somemodule'
    }
}
```

### Module Lifecycle
Modules are designed to be pseudo plugins. The reason for the abstraction is easier lifecycle management.
Typically, Bukkit plugins can only be disabled/enabled/reloaded at runtime if: 1) The plugins build this functionality in or 2) You have PlugManX installed (which can be faulty)
KCommon builds the functionality into each module and allows admins to control modules during runtime via
`/km reload`, `/km enable`, and `/km disable`.

All modules are singleton. Only one instance of the module ever exists at a time.
To obtain the instance of the module, there are a few options.
The safest and most straightforward:
```java
// Straight forward
SomeModule module = Modules.getModule(SomeModule.class);
// Using the module name. Can be dangerous if two plugins register the same module name
module = Modules.getModule("somemodule");
// Using module namespaced key
module = Modules.getModule(new KNamespacedKey("plugin-name", "somemodule"));
```
There is also the magic semi-reflective
```java
SomeModule module = SomeModule.get();
```

The Module class provides a static utility method:
```java
public static <T extends Module> T get(@NotNull T... unused) {
    return (T) Modules.getModule((Class<? extends Module>) unused.getClass().getComponentType());
}
```

Which allows you to retrieve the module if you call it EXACTLY as shown above. If you simply use `SomeModule.get()` without the proper generic type provided, an error will occur.

## Configuration
Each module has its own config places into the resources/(module-name)/config.yml.
Use the `SomeModuleConf` class to add fields that reflect the items in the config.

For example, if I wanted to represent this config in `SomeModuleConf`:
```yaml
some-integer-option: 100
another-string: "Hello World!"
```
I could do this:
```java
public class SomeModuleConf implements ConfigClassSource {
    @Conf("This is a comment that will appear in the config attached to this option!")
    public static int someIntegerOption = 100;
    @Conf({
        "This is a comment that will appear in the config attached to this option!",
        "This is another comment for multiline!"
    })
    public static String anotherString = "Hello World!";
}
```
Note that `@Conf` is important as that is what links the field to the config.

### Creating serializable types
To represent a complex type, you can create a structure class that implements CASerializable.
```java
// Requires a no-args constructor for serialization
public class ComplexType implements CASerializable {
    // This is a special field that is not serialized to the config normally.
    // This is the key that the complex type is stored under in the config.
    private String _key;
    private int complexPart1;
    private double complexPart2;
    
    @Override
    public void onDeserialize() {
        // Optional method called when deserialized
    }
    
    @Override
    public void onDeserialize(ConfigPrimitive primitive /* The primitive the object was created from */) {
        // Optional method called when deserialized
    }
    
    @Override
    public void onSerialize() {
        // Optional method called when serialized
    }
}
```
`@Conf` is not required for types that implement CASerializable.

### Built in configurable types
There are many types built into KCommon that support automatic serialization.
These types have a type adapter built into KCommon, so CASerializable is not required.
Refer to the `com.golfing8.kcommon.config.adapter` package in the KCommon library to see if a type is supported.
Prefer already serializable types to adding new serializable types.

## Commands
Commands should be directly linked to modules as follows.
When creating commands in the context of a module, ALWAYS use `MCommand<SomeModule>`. Never implement CommandExecutor directly.
```java
@Cmd(
        name = "somemodule",
        description = "The main command for somemodule",
        aliases = {"sm"},
        forPlayers = false, // Optional, if true makes it so only players can execute. If false, everyone can.
        async = false // Optional, if true this will run the command async. Should only be done in cases that the command is intensive.
)
public class SomeModuleCMD extends MCommand<SomeModule> {
    @Override
    protected void onRegister() {
        // Register subcommands and arguments here.
        
        // An argument for an online player. Tab completions are provided automatically!
        // There are many built in argument types in the CommandArguments class.
        addArgument("player", CommandArguments.PLAYER);
        // An argument for a positive integer. You can provide a third argument for auto-completion on any command.
        BuiltCommandArgument arg = addArgument("amount", CommandArguments.POSITIVE_INTEGER, sender -> 1);
        // Can set this to true, false, or null. 
        // true -> Only players can autofill (useful for player arguments)
        // false -> Only console can autofill
        // null -> Anyone can autofill (default behavior)
        arg.setAutoFillPlayersOnly(true);
        // Useful for commands that allow for targeting specific players.
        // This allows for admins to target the command at other players but normal players can only use it for themselves.
        arg.setRequiredPermissionExtension("extension");
        
        // We can register subcommands alongside arguments
        addSubCommand(new SomeModuleSubCommand());
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        // Read the arguments in order with subsequent calls to .next()
        Player player = context.next();
        int amount = context.next();
        
        // Execute command logic here.
    }
}
```
Both methods are optional. If `execute` is missing, the command acts as a directory and can only display the help menu for sub commands.

## Menus
KCommon builds in a menu system built on top of the `MenuBuilder` class.
Similar to JS/HTML frameworks, the idea is to separate structure into YML and functionality into Java.
Here's a simple example menu structure in YML:
```yaml
virtual-inventory-menu:
  title: '&aVirtual Inventory'
  # You may provide an inventory type (HOPPER, DROPPER)
  # OR
  # size: 54 # (number divisible by 9)
  type: HOPPER
  # Fills unfilled slots with a filler item. By default, this is a light gray stained glass pane
  use-filler-item: true
  # Overrides default filler item
  filler-item:
    type: GRAY_STAINED_GLASS_PANE
    # Make name transparent
    name: '&7'
  filler-shape:
    type: OUTLINE # Can be OUTLINE (perimeter of rectangle), RECTANGLE (filled rectangle), and POINTS
    low-slot: 0
    high-slot: 4
    # If using points, define each slot under `slots`
  other-slots:
    info:
      slot: 0
      type: PAPER
      name: '&eVirtual Inventory'
      lore:
        - '&7Claim your virtual inventory'

  special-slots:
    claim-none-available:
      slot: 2
      type: BARRIER
      name: "&cVirtual Inventory is Empty"
    claim:
      slot: 2
      type: BUNDLE
      name: '&aClaim Inventory'
      lore:
        - '&aMoney: ${MONEY}'
        - '&eXP: {XP}'
        - '&aItems: &e{ITEMS}'
```
The structure of the menu is defined in three primary areas for simple menus.

### Main Section
You can configure `title`, `type`, `size`, `use-filler-item`, `filler-item`, and `filler-shape`.

### special-slots
Defines the functional item bindings of the menu. In code, we bind to the special item keys as follows:
```java
MenuBuilder builder = module.getMainConfig().getOrLoad("virtual-inventory-menu", MenuBuilder.class).orElseThrow();
if (profile.getItemVaultItems().isEmpty()) {
    builder.bindTo("claim-none-available", event -> {});
} else {
    builder.bindTo("claim", event -> {
        // ... Implementation code
        // Refreshes the appearance of the menu. Very important if the data in the placeholders change!
        refresh();
    });
    builder.specialPlaceholders("claim", Placeholder.compileCurlyTrusted(
            "MONEY", StringUtil.parseMoney(profile.getStoredMoney().doubleValue()),
            "XP", StringUtil.parseCommas(profile.getStoredXp()),
            "ITEMS", profile.getItemVaultItems().size()
    ));
}
```
If the Java code binds to a specific special-item, it will appear in the built menu. If it does not bind it, it will not appear.

### other-slots
These items will always appear in the menu and have no functionality.

### Menu Containers
In Java code, KCommon provides a few abstractions for working with the MenuBuilder class.

#### PlayerMenuContainer
A very simple container for building menus for players.
YAML Structure: Same as shown above.
An example:
```java
public class VirtualInventoryMenu extends PlayerMenuContainer {
    // If relevant, get the module
    private final VirtualInventoryModule module = VirtualInventoryModule.get();
    
    public VirtualInventoryMenu(Player player) {
        super(player);
    }

    @Override
    protected Menu loadMenu() {
        // Create a menu builder
        MenuBuilder
    }
}
```

#### SuppliedPagedMenuContainer
A paged menu container for displaying a number of elements.
YAML Structure:
```yaml
warp-menu:
  title: '&aPlayer Warps'
  use-filler-item: true
  size: 54
  # Defines the shape the supplied elements appear on each page
  element-section-shape:
    type: RECTANGLE
    low-slot: 10
    high-slot: 43
  filler-item:
    type: GRAY_STAINED_GLASS_PANE
    name: '&7'
  filler-shape:
    type: OUTLINE
    low-slot: 0
    high-slot: 53
  favorite-warp-format:
    type: PLAYER_HEAD
    name: '&b** &e{NAME} &b**'
    lore:
      - '&7{DESCRIPTION}'
      - '&aOwner: &e{OWNER}'
      - '&aUsed: &e$commas{{USE_COUNT}}'
      - '&aFavorites: &e$commas{{FAVORITES}}'
      - '&aWorld: &e{WORLD}'
      - '&cRight click to unfavorite'
      - '&aLeft click to teleport'
  normal-warp-format:
    type: PLAYER_HEAD
    name: '&e{NAME}'
    lore:
      - '&7{DESCRIPTION}'
      - '&aOwner: &e{OWNER}'
      - '&aUsed: &e$commas{{USE_COUNT}}'
      - '&aFavorites: &e$commas{{FAVORITES}}'
      - '&aWorld: &e{WORLD}'
      - '&aRight click to favorite'
      - '&aLeft click to teleport'
  sort-prefix:
    selected: "&b"
    unselected: "&f"
  special-slots:
    sort:
      slot: 49
      type: PAPER
      name: '&bSorting'
      lore:
        - "%SORTING%"
```
The Java code will load the formats and call `forEachElementOnPage` to build the menu.

## Placeholders/PlaceholderAPI
Add Placeholders for PlaceholderAPI in the following way inside the module class.
```java
@ModuleInfo(
        name = "somemodule",
        configSources = SomeModuleConf.class,
        langSources = SomeModuleLang.class
)
public class SomeModule extends Module {
    @Override
    public void onEnable() {
        // Register the placeholder to the module here and give it a description.
        addPlaceholder(new KPlaceholderDefinition("simple_placeholder", "Returns information about a simple placeholder"), (player, args) -> {
            // 'player' is an instance of OfflinePlayer.
            // 'args' gives us the rest of the placeholder.
            // For example, if the placeholder is %<plugin>_somemodule_simple_placeholder_arg1_arg2%, then args = {arg1, arg2}.
            // Use 'args' for formatting options in scenarios where that is relevant.
            return "This is a simple placeholder!";
        });
    }
    
    @Override
    public void onDisable() {
        
    }
}
```

## Persistence
Data should be persisted through reboots via the KCommon DataManager interface. 
Using our SomeModule example:
```java
@ModuleInfo(
        name = "somemodule",
        configSources = SomeModuleConf.class,
        langSources = SomeModuleLang.class
)
public class SomeModule extends Module implements DataManagerContainer {
    private static final String SINGLETON_KEY = "singleton";
    
    @Override
    public void onEnable() {
        // Register a data manager for persistence (this uses GSON)
        addDataManager("some-unique-data-key-type", SomePersistedData.class);
        
        // For singleton patterns, typically do this:
        SomePersistedData persistentData = getOrCreate(SINGLETON_KEY, SomePersistedData.class);
    }

    @Override
    public void onDisable() {

    }
}
```
Persist data for things like player profiles where statistics or information must be tracked through the module lifecycle.

There are two base classes used for data persistence. `AbstractSerializable` and `SenderSerializable`.

### AbstractSerializable
Used for persistent data not tied to any player. These can use any type of string key for storage.
Use these for singleton runtime data, block data, location data, or anything not tieable to an individual player.

### SenderSerializable
Used for profile data for a player. If data can be tied to a player, use a SenderSerializable instance instead of an AbstractSerializable instance.

## Agent Guidance
When editing this project:
* Prefer existing module, menu, command, config, and persistence patterns over introducing new abstractions
* Keep YAML structure and Java bindings synchronized
* Treat `SomeModule`, `SomeModuleConf`, and related types in this document as templates, not literal required names
* When replacing examples, preserve the lifecycle and framework semantics shown here
* Favor minimal, targeted changes that fit the surrounding KCommon-based architecture