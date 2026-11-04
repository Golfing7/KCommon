# Menus

KCommon menus separate layout in YAML from behavior in Java. A menu section
can be loaded as a `MenuBuilder`, then bound to a player through a menu
container.

## Define the menu in YAML

Put this in the module's `config.yml`:

```yaml
greeting-menu:
  title: "&aGreetings"
  size: 27
  use-filler-item: true
  filler-item:
    type: GRAY_STAINED_GLASS_PANE
    name: "&7"
  special-slots:
    greet:
      slot: 13
      type: PAPER
      name: "&eGreet {PLAYER}"
      lore:
        - "&7Click to greet the player."
    greet-disabled:
      slot: 13
      type: BARRIER
      name: "&cGreeting unavailable"
  other-slots:
    title:
      slot: 4
      type: BOOK
      name: "&aGreeting menu"
```

`special-slots` are functional bindings. They appear only when Java binds the
same key. `other-slots` always appear and do not have click behavior.

## Bind the menu

`PlayerMenuContainer` owns a menu for one player. Rebuild the builder in
`loadMenu()` so `refresh()` can re-evaluate dynamic values:

```java
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import org.bukkit.entity.Player;

import java.util.Collections;

public final class GreetingMenu extends PlayerMenuContainer {
    private final GreetingsModule module = GreetingsModule.get();
    private final GreetingProfile profile;

    public GreetingMenu(Player player) {
        super(player);
        this.profile = module.profile(player.getUniqueId());
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = module.getMainConfig()
                .getOrLoad("greeting-menu", MenuBuilder.class)
                .orElseThrow(() -> new IllegalStateException("greeting-menu is missing"));

        if (profile.canGreet()) {
            builder.bindTo("greet", event -> {
                profile.recordGreeting();
                refresh();
            });
            builder.specialPlaceholders("greet", Collections.singleton(
                    Placeholder.curlyTrusted("PLAYER", getPlayer().getName())
            ));
        } else {
            builder.bindTo("greet-disabled", event -> {});
        }
        return builder.buildSimple();
    }
}
```

Open it with:

```java
new GreetingMenu(player).open();
```

Call `refresh()` after changing values used by item names, lore, or
placeholders. `MenuBuilder` also supports global placeholders, locked slots,
custom filler shapes, click actions, and dynamic menus.

For menus that edit inventory contents, use `postCloseRunnable` to persist the
final contents after the inventory closes. Keep the menu's target object as a
constructor field, as in the example above, so every rebuild binds against the
same state.

## Paged menus

Use `SuppliedPagedMenuContainer<T>` when the menu displays a collection. Set
the source and count, then implement `loadMenu(MenuBuilder)` and call
`forEachElementOnPage` or `forEachElementOnPageWithIndex`.

```yaml
greeting-list-menu:
  title: '&aGreetings'
  use-filler-item: true
  size: 54
  # Defines the shape the supplied elements appear on each page
  element-section-shape:
    type: RECTANGLE # RECTANGLE, OUTLINE, or POINTS
    low-slot: 10
    high-slot: 43
  filler-item:
    type: GRAY_STAINED_GLASS_PANE
    name: '&7'
  filler-shape:
    type: OUTLINE
    low-slot: 0
    high-slot: 53
  favorite-greeting:
    type: PLAYER_HEAD
    name: '&b** &e{NAME} &b**'
    lore:
      - '&7{DESCRIPTION}'
      - '&aOwner: &e{OWNER}'
      - '&cRight click to unfavorite'
      - '&aLeft click to teleport'
  normal-greeting-format:
    type: PLAYER_HEAD
    name: '&e{NAME}'
    lore:
      - '&7{DESCRIPTION}'
      - '&aOwner: &e{OWNER}'
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

```java
public final class GreetingListMenu extends SuppliedPagedMenuContainer<String> {
    private final GreetingsModule module = GreetingsModule.get();
    
    public GreetingListMenu(Player player, List<String> greetings) {
        super(player);
        
        // It is important to set the parent section of the supplied paged menu container before anything else.
        ConfigurationSection parentSection = module.getMainConfig().getOrLoad("greeting-list-menu", ConfigurationSection.class).orElseThrow();
        setParentSection(parentSection);
        
        
        setElements(greetings);
    }

    @Override
    protected Menu loadMenu(MenuBuilder builder) {
        forEachElementOnPage((coordinate, greeting) -> {
            // Add the item and action for this coordinate.
        });
        return builder.buildSimple();
    }
}
```

Paged menus use chest inventories. Configure
`element-section-shape` when the items should occupy a custom set of slots;
the default leaves the bottom row for navigation.

For data supplied by another service, snapshot the current collection with
`new ArrayList<>(items)` in the constructor. Load reusable
`ItemStackBuilder` formats there, then use `loadMenu(MenuBuilder)` to apply
global placeholders and build each element. Before performing a click action,
recheck that the element is still available; a menu can remain open while the
underlying data changes.

## Hytale

The Hytale build has the same split. A menu section loads as a Hytale
`MenuBuilder`, and `PlayerMenuContainer` or `PagedMenuContainer<T>` owns it
for one player. The menu is sent to the client as one inline UI document, so
plugins need no `.ui` asset files.

Hytale menus are trees rather than slots. Put them in the module's
`menus.yml` and load them with `module.getConfig("menus")`:

```yaml
greeting-menu:
  title: "&aGreetings"
  width: 500
  height: 400
  # Properties of the frame's content Group.
  content:
    layout-mode: Top
    padding: {full: 16}
  elements:
    greetings:
      layout-mode: TopScrolling
      flex-weight: 1
      scrollbar-style: '@DefaultScrollbarStyle'
    close:
      type: TextButton
      special: true
      style: '@CancelTextButtonStyle'
      anchor: {height: 44}
      text: Close
  templates:
    greeting:
      layout-mode: Left
      anchor: {height: 40}
      children:
        name:
          type: Label
          text: "&e{NAME}"
          style: {font-size: 16, text-color: '#ffffff'}
          flex-weight: 1
        greet:
          type: TextButton
          style: '@DefaultTextButtonStyle'
          text: Greet
```

```java
public final class GreetingMenu extends PlayerMenuContainer {
    private final GreetingsModule module = GreetingsModule.get();

    public GreetingMenu(PlayerRef player) {
        super(player);
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = module.getConfig("menus")
                .getOrLoad("greeting-menu", MenuBuilder.class)
                .orElseThrow(() -> new IllegalStateException("greeting-menu is missing"));

        for (Greeting greeting : module.getGreetings()) {
            builder.addRow("greetings", "greeting")
                    .placeholders("NAME", greeting.getName())
                    .bindTo("greet", event -> {
                        greeting.send(event.getPlayerRef());
                        event.refresh();
                    });
        }
        return builder.bindTo("close", MenuClickEvent::close).build();
    }
}
```

Open it with `new GreetingMenu(playerRef).open()`. Every container method may
be called from any thread; the work runs on the player's world thread, and so
do click actions.

- Elements default to `Group`. Types and properties are Hytale's markup names
  in kebab case: `Label`, `TextButton`, `Button`, `ItemIcon`, `ItemSlot`,
  `AssetImage`, `ProgressBar`, `TextField`, plus `Title` (a label styled like
  the game's titles) and `StyledText` (one label per character, for
  gradients).
- The Hytale client disconnects a player when a page holds markup it doesn't
  know. KCommon checks element types, properties and values when the menu
  loads, and placeholder values when it builds, and refuses to open a menu
  that would break.
- A value like `'@DefaultTextButtonStyle'` references `Common.ui`;
  `'Pages/Foo.ui@Name'` references another document. Use
  `{ref: '@SecondaryButtonStyle', sounds: '@ButtonSounds'}` to add sounds to a
  referenced style.
- `special` elements only appear once Java binds or `show`s them. Use
  `set`, `visible`, `disabled` and `text` to change an element from Java.
- Rows come from `templates`, added with `addRow(container, template)`. Each
  row has its own placeholders and bindings; its placeholders win over the
  menu's.
- `PagedMenuContainer` reads `elements-per-page`, adds `{PAGE}` and
  `{MAX_PAGE}`, and `bindPageButtons` wires previous and next buttons.

