# Configuration

KCommon creates a module's main configuration under:

```text
<plugin data folder>/<module name>/config.yml
```

On first load, files stored in the plugin jar under
`src/main/resources/<module name>/` are copied into that directory. Put a
default `config.yml` there when you want to ship comments or structured
defaults.

## Reflective configuration

Use a `ConfigClassSource` for fields that should be loaded into the module
configuration. Every field must have `@Conf` because module config sources
require the annotation.

```java
package com.example.example.greetings;

import com.golfing8.kcommon.config.generator.Conf;
import com.golfing8.kcommon.config.generator.ConfigClassSource;

public final class GreetingsConfig implements ConfigClassSource {
    @Conf("Text shown when a player is greeted.")
    public static String greetingMessage = "&aWelcome, {PLAYER}!";

    @Conf("Whether the greeting is enabled.")
    public static boolean enabled = true;
}
```

KCommon maps Java field names to YAML paths, so `greetingMessage` becomes
`greeting-message`. The defaults are written to `config.yml` and existing
server values are loaded into the fields.

The `@Conf` annotation also supports a YAML label and a separate module
config file:

```java
@Conf(
        value = "The maximum number of greetings to show.",
        label = "max-greetings",
        config = "limits"
)
public static int maxGreetings = 3;
```

This value is stored in `<module data folder>/limits.yml`. Keep the
`configSources` entry in `@ModuleInfo` synchronized with the source class.

## Reading structured values

For values with a registered KCommon adapter, load from the module's
configuration section:

```java
MenuBuilder menu = getMainConfig()
        .getOrLoad("greeting-menu", MenuBuilder.class)
        .orElseThrow(() -> new IllegalStateException("greeting-menu is missing"));
```

`getOrLoad` can load a bundled resource into the data folder when necessary.
Use `getConfig("name")` for an additional YAML file or
`loadConfigGroup("directory")` for a directory of related files.

Before creating a custom adapter, check the
[`config.adapter` package](https://github.com/Golfing7/KCommon/tree/main/KCommon/src/main/java/com/golfing8/kcommon/config/adapter)
for an existing type adapter.

## Custom serializable types

Implement `CASerializable` when a value is a structured object that KCommon
does not already support:

```java
public final class GreetingFormat implements CASerializable {
    private String prefix = "&a";
    private String message = "Welcome!";

    @Override
    public void onDeserialize() {
        // Normalize or validate fields after loading when needed.
    }
}
```

Custom serializable types require a no-argument constructor. Use
`CASerializable.Options` for advanced behavior such as flattened values,
delegated paths, config mode, or polymorphic type resolution.

## Configuration rules

- Treat configuration as user input and validate values before using them.
- Keep Bukkit API access on the server thread unless the operation is safe
  off-thread.
- Store only defaults in the jar. User-edited values live in the plugin data
  folder.
- Reload configuration through the module lifecycle instead of caching stale
  values across `onDisable()`.
