# Language and messages

KCommon stores module messages in:

```text
<plugin data folder>/<module name>/lang.yml
```

Plugin-wide messages owned directly by `KPlugin` use
`<plugin data folder>/kore-lang.yml`.

## Define configurable messages

Use `@LangConf` on non-null `Message` fields in a module, command, or other
`LangConfigContainer`:

```java
import com.golfing8.kcommon.config.lang.LangConf;
import com.golfing8.kcommon.config.lang.Message;

public final class GreetingsModule extends Module {
    @LangConf
    private Message greetedMessage = new Message("&aHello, {PLAYER}!");

    @Override
    public void onEnable() {
        // The field is replaced with the configured Message after loading.
    }

    @Override
    public void onDisable() {
    }
}
```

The field name becomes the YAML key. Use `@LangConf(path = "messages")` to
place it below a path. `Message` can represent a string, multiple lines, an
action bar, a title, sounds, or a paged response.

```yaml
greeted-message:
  message:
    - "&aHello, {PLAYER}!"
    - "&7Welcome to the server."
  actionbar: "&eYou were greeted."
  title:
    title: "&aWelcome"
    subtitle: "&7Enjoy your visit."
```

## Send messages

Prefer configurable messages over hard-coded output.
The methods `sendConfigMessage` and `sendDefaultMessage` should only be used when the context of a Module's language config 
(or a Message instance annotated with @LangConf) is unavailable.

`sendDefaultMessage` is useful for one-off defaults, but
`@LangConf` or use of the `*Lang` enum makes the complete language surface
discoverable and editable.

For larger message sets, declare a `LangConfigEnum` in `@ModuleInfo`:

```java
public enum GreetingsLang implements LangConfigEnum {
    GREETED(new Message("&aHello!")),
    COMMANDS$RELOAD$DONE(new Message("&aReloaded greetings."));

    private Message message;

    GreetingsLanguage(Message message) {
        this.message = message;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public void setMessage(Message message) {
        this.message = message;
    }
}
```

The enum key `GREETED` is stored as `greeted`. KCommon converts underscores to
hyphens and `$` to nested YAML paths, so
`COMMANDS$RELOAD$DONE` is stored as `commands.reload.done`. Add the enum to
`langSources` so KCommon loads it during the module lifecycle.
