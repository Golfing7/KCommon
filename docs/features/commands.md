# Commands

Module commands should extend `MCommand<YourModule>`. Register them with
`addCommand` from `onEnable()`:

```java
package com.example.example.greetings;

import com.golfing8.kcommon.command.CommandContext;
import com.golfing8.kcommon.command.Cmd;
import com.golfing8.kcommon.command.MCommand;
import com.golfing8.kcommon.command.argument.CommandArguments;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Cmd(
        name = "greet",
        aliases = {"hello"},
        description = "Greet an online player",
        forPlayers = true
)
public final class GreetingsCommand extends MCommand<GreetingsModule> {
    @Override
    protected void onRegister() {
        addArgument("player", CommandArguments.PLAYER);
    }

    @Override
    protected void execute(@NotNull CommandContext context) {
        Player target = context.next();
        getModule().sendConfigMessage(
                context.getSender(),
                "greeted",
                Placeholder.curlyTrusted("PLAYER", target.getName())
        );
    }
}
```

```java
@Override
public void onEnable() {
    addCommand(new GreetingsCommand());
}
```

`MCommand` finds its module from the generic type and uses the module's
language config and permission prefix. KCommon unregisters the command when
the module is disabled, so register it on every enable.

Commands can own their validation messages with `@LangConf`, while still
sharing the module's `lang.yml`:

```java
@LangConf
private Message invalidTarget = new Message("&cThat player cannot be greeted.");

// In execute(...)
if (target == null) {
    invalidTarget.send(context.getSender());
    return;
}
```

Use `getModule().getLogger()` for administrative or state-changing command
actions. This keeps user feedback configurable while leaving an audit trail
in the server log.

## Arguments and subcommands

Use the built-in argument types for validation and tab completion:

```java
addArgument("amount", CommandArguments.POSITIVE_INTEGER);
addArgument("enabled", CommandArguments.BOOLEAN_STATE);
addArgument("player", CommandArguments.PLAYER);
```

Read arguments in declaration order with `context.next()`. The context also
provides `getArg(index)`, `getInt(index)`, `getPlayer(index)`, and
`joinRemainingToString()`.

For choices loaded from configuration, use a map-backed argument so values
receive validation and tab completion from the same source:

```java
addArgument("format", CommandArgument.fromMap(
        "a greeting format",
        GreetingsConfig.formats
));
```

Build or validate the map before registering the command, and handle an empty
map as a configuration error instead of exposing a command with no valid
choices.

For context-sensitive tab completion, keep the argument type reusable and
configure the returned argument in `onRegister()`:

```java
BuiltCommandArgument amount =
        addArgument("amount", CommandArguments.POSITIVE_INTEGER, sender -> 1);
amount.setAutoFillPlayersOnly(true);
amount.setRequiredPermissionExtension("admin");
```

Commands without an `execute` implementation act as directories. Add a
subcommand from `onRegister()`:

```java
addSubCommand(new GreetingsReloadCommand());
```

## Permissions and execution

The default `@Cmd` permission is generated from the plugin, module, and
command names:

```text
<plugin>.<module>.command.<command>
```

Set `permission` explicitly when the command must use a stable external
permission. Use `forPlayers = true` to reject console senders. Set
`async = true` only when the entire command body is safe away from the
Bukkit server thread; Bukkit entity, world, and inventory operations should
remain synchronous.

## Language messages

Commands share their module's language config. Prefer
`sendConfigMessage(sender, "key", placeholders...)` over hard-coded output.
See [Language and messages](language.md) for defining and configuring those
keys.
