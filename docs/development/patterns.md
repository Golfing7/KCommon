# Design patterns for production modules

The examples in this guide use neutral names and invented values. They show
patterns that scale from a small feature to a large multi-module plugin
without copying application-specific names, integrations, or data.

## Organize by feature

Keep each feature in one package instead of grouping the entire plugin by
technical layer:

```text
com.example.plugin.module.feature/
  FeatureModule.java
  FeatureConfig.java
  FeatureLanguage.java
  cmd/
  data/
  listener/
  menu/
  struct/
```

Not every feature needs every directory. The benefit is that a module's
configuration, commands, data, listeners, menus, and domain types are easy to
find and can be enabled or disabled together.

## Use the module as the composition root

Initialize derived configuration, register persistence, and then attach the
feature's commands, listeners, tasks, and placeholders from `onEnable()`:

```java
@Override
public void onEnable() {
    FeatureConfig.init();

    DataManager<FeatureProfile> profiles =
            addDataManager("feature-profiles", FeatureProfile.class);
    profiles.setStrictSaving(true);

    addCommand(new FeatureCommand());
    addSubListener(new FeatureListener(this));
    addTask(this::updateFeatureState).startTimer(0, 20);

    addPlaceholder(
            new KPlaceholderDefinition("feature_value", "Current feature value"),
            (player, args) -> Integer.toString(currentValue(player))
    );
}
```

This gives every runtime resource the same owner and makes a module reload
predictable. KCommon automatically removes module commands, listeners, tasks,
placeholders, and data-manager registrations during disable.

## Separate configuration from runtime state

`FeatureConfig` should contain defaults and user-editable values. An `init()`
method can validate those values and construct lookup maps, reverse indexes,
or other derived state. Re-run it on every enable; do not treat a derived
static field as permanent application state.

Persisted domain objects should own durable data and expose operations such as
`increment`, `add`, or `remove` that call `change()`. Keep Bukkit handles,
listeners, tasks, and caches out of serialized fields, then reattach those
runtime resources after loading.

## Gate optional integrations twice

Use both plugin metadata and module metadata when an integration affects a
feature:

```yaml
softdepend:
  - OptionalPlugin
```

```java
@ModuleInfo(
        name = "feature",
        pluginDependencies = "OptionalPlugin"
)
public final class FeatureModule extends Module {
    // ...
}
```

`softdepend` gives Bukkit a useful load order. `pluginDependencies` tells
KCommon whether to register the module at all. Inside `onEnable()`, still
check optional capabilities before registering integration-specific listeners
or tasks.

## Keep thread boundaries explicit

Use `ModuleTask` for asynchronous work and switch back to the main thread
before touching Bukkit objects:

```java
addTask(this::loadRecords).startLaterAsync(0);

addTask(() -> {
    for (Player player : Bukkit.getOnlinePlayers()) {
        updatePlayerView(player);
    }
}).startTimer(0, 20);
```

Do not access entities, worlds, inventories, or most Bukkit services from an
async task. If async work produces a result for the server, schedule a new
synchronous module task to apply it.

## Keep user output configurable

Commands and menu actions should use `@LangConf` fields or a module
`LangConfigEnum` instead of hard-coded player-facing text. Use placeholders
for values and log administrative mutations with the module logger. This
keeps the language file editable while preserving an operational audit trail.

## Make menu containers stateful, not global

Pass the player and feature target into a menu container's constructor. Load
the builder from configuration in `loadMenu()`, bind only the actions valid for
the current state, and call `refresh()` after a state-changing click. Avoid
static menu instances; a module reload must not leave viewers holding objects
from the previous lifecycle.
