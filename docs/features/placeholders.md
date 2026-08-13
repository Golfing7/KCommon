# Placeholders

Modules can register PlaceholderAPI placeholders in `onEnable()`. The
placeholder label is local to the module; KCommon prefixes it with the plugin
and module names.

```java
import com.golfing8.kcommon.hook.placeholderapi.KPlaceholderDefinition;

@Override
public void onEnable() {
    addPlaceholder(
            new KPlaceholderDefinition(
                    "greeting_count",
                    "Returns the number of greetings for the player"
            ),
            (player, args) -> {
                if (player == null) {
                    return "0";
                }
                return Integer.toString(profile(player.getUniqueId()).getGreetings());
            }
    );
}
```

For a plugin named `ExamplePlugin`, module `greetings`, and label
`greeting_count`, the resulting PlaceholderAPI identifier is:

```text
%exampleplugin_greetings_greeting_count%
```

The function receives an `OfflinePlayer`, so it must handle players who are
not currently online. Arguments after the registered label are passed in
`args`. For example, a label of `top` can accept
`%exampleplugin_greetings_top_week%` with `args = ["week"]`.

Register placeholders again during every module enable; KCommon removes them
when the module is disabled. Use `addRelPlaceholder` when the value depends on
two players.

## Configuration-defined placeholders

For a configurable set of values, store definitions in a typed map and
register the validated entries during `onEnable()`:

```java
for (Map.Entry<String, TimerDefinition> entry :
        TimerConfig.timers.entrySet()) {
    String label = entry.getKey();
    TimerDefinition definition = entry.getValue();

    if (definition == null || !definition.isValid()) {
        getLogger().warning("Skipping invalid timer: " + label);
        continue;
    }

    addPlaceholder(
            new KPlaceholderDefinition(label, "Configured timer value"),
            (player, args) -> definition.displayValue()
    );
}
```

Validate labels as well as values before registration. Skipping an invalid
definition with a warning is safer than publishing a placeholder that returns
misleading data.

Check registered values from the server with:

```text
/km placeholders greetings
```
