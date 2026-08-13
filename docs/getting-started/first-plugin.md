# First plugin

## Create the plugin class

`KPlugin` owns the Bukkit `onLoad`, `onEnable`, and `onDisable` lifecycle
methods. They are final, so use the `*Inner` hooks for plugin-wide setup only
when a module is not the right place for the work.

```java
package com.example.example;

import com.golfing8.kcommon.KPlugin;

public final class ExamplePlugin extends KPlugin {
    @Override
    public void onLoadInner() {
        // Optional plugin-wide setup.
    }

    @Override
    public void onEnableInner() {
        // Optional plugin-wide setup before modules are discovered.
    }

    @Override
    public void onDisableInner() {
        // Optional plugin-wide cleanup.
    }
}
```

KCommon creates the plugin language config, menu manager, PlaceholderAPI hook,
and module manifest before it initializes modules.

## Create a module

Modules need a no-argument constructor. The protected constructor supplied by
`Module` reads `@ModuleInfo`, registers the module, and connects it to the
owning `KPlugin`.

```java
package com.example.example.greetings;

import com.golfing8.kcommon.module.Module;
import com.golfing8.kcommon.module.ModuleInfo;

@ModuleInfo(
        name = "greetings",
        configSources = GreetingsConfig.class
)
public final class GreetingsModule extends Module {
    @Override
    public void onEnable() {
        // Register commands, listeners, tasks, and placeholders here.
    }

    @Override
    public void onDisable() {
        // Release resources owned by this module.
    }
}
```

Place the module in the package of your plugin or one of its subpackages.
KCommon discovers annotated module classes from that classloader during plugin
enable. The module is then enabled unless its saved manifest state is disabled.

The next pages show how to add the feature pieces that belong in
`onEnable()`. For a complete feature, read [Modules](../features/modules.md)
and [Configuration](../features/configuration.md) next.
