# Build plugins with KCommon

KCommon is a Bukkit/Paper plugin library that organizes a plugin into independently
manageable **modules**. A module owns its lifecycle, configuration, commands,
menus, placeholders, and persisted data. Modules can be enabled, disabled, and
reloaded without restarting the server.

This guide walks through creating a plugin that uses KCommon at runtime. It
assumes familiarity with Java, Gradle, and Bukkit or Paper plugin development.

## The KCommon model

```text
YourPlugin extends KPlugin
    |
    +-- @ModuleInfo module
            +-- config.yml and lang.yml
            +-- MCommand commands
            +-- Menu containers
            +-- DataManager persistence
            +-- PlaceholderAPI placeholders
```

The plugin class provides the integration point. Feature code belongs in
modules, which KCommon discovers automatically from the plugin classpath.

## Recommended path

1. Add KCommon as a `compileOnly` dependency and declare `KCommon` in
   `plugin.yml`.
2. Extend `KPlugin` and create an annotated `Module`.
3. Add configuration and a module command.
4. Add menus, persistence, placeholders, or language messages as the feature
   needs them.
5. Build and test against a development server, then reload the module with
   `/kmodules reload <module>` (or `/km reload <module>`).

Start with [Prerequisites and dependency](getting-started/dependency.md).
