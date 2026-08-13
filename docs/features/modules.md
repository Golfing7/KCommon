# Modules

A module is KCommon's unit of feature ownership. `Module` implements Bukkit's
listener interface and manages resources registered through the module. Put
feature setup in `onEnable()` and cleanup in `onDisable()`.

## Lifecycle

KCommon follows this lifecycle:

1. Discover the class annotated with `@ModuleInfo`.
2. Construct and register its singleton instance.
3. Load the module's configuration and language files.
4. Register the module listener and run `onEnable()`.
5. Register module placeholders and save newly created language values.

When a module is disabled, KCommon unregisters its listeners and commands,
cancels module tasks, removes placeholders, shuts down data managers, and
clears module-owned configuration state. Register lifecycle-owned resources
inside `onEnable()` so reloads rebuild them correctly.

```java
@Override
public void onEnable() {
    addCommand(new GreetingsCommand());
}

@Override
public void onDisable() {
    // Close resources that are not registered with the module.
}
```

The module itself is a Bukkit listener, so its `@EventHandler` methods are
registered automatically while it is enabled. Use `addSubListener` for a
separate listener whose lifetime should be controlled by the module.

## Module metadata

`@ModuleInfo` supports more than a name:

```java
@ModuleInfo(
        name = "greetings",
        moduleDependencies = {"economy"},
        pluginDependencies = {"PlaceholderAPI"},
        minimumMajorVersion = 1,
        configSources = GreetingsConfig.class,
        langSources = GreetingsLanguage.class
)
public final class GreetingsModule extends Module {
    // ...
}
```

- `moduleDependencies` delays initialization until named KCommon modules are
  available.
- `pluginDependencies` prevents registration when a named Bukkit plugin is
  not enabled.
- The version fields gate a module by the server's major or minor version.
- `configSources` adds reflective configuration classes.
- `langSources` loads `LangConfigEnum` values into the module language file.

Leave optional metadata at its default. Module names are lowercased when
looked up and should be unique within a plugin.

## Getting a module

KCommon keeps one module instance per module class. The type-safe lookup is:

```java
GreetingsModule module = Modules.getModule(GreetingsModule.class);
```

The reflective shorthand is also available:

```java
GreetingsModule module = GreetingsModule.get();
```

Use `Modules.getModule(String)` only when a namespaced class lookup is not
possible, because two plugins can register the same short module name.

## Enable, disable, and reload

KCommon exposes module management through `/kmodules` and its `/km` alias:

```text
/km list
/km enable greetings
/km disable greetings
/km reload greetings
/km commands greetings
/km placeholders greetings
```

The enabled or disabled state is stored in `module-manifest.json` in the
plugin data folder. A reload invokes the module's disable and enable lifecycle
again; it does not reload the Bukkit plugin.
