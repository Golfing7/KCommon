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

    public GreetingMenu(Player player) {
        super(player);
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = module.getMainConfig()
                .getOrLoad("greeting-menu", MenuBuilder.class)
                .orElseThrow(() -> new IllegalStateException("greeting-menu is missing"));

        builder.bindTo("greet", event -> {
            event.getWhoClicked().sendMessage("Hello!");
            refresh();
        });
        builder.specialPlaceholders(
                "greet",
                Collections.singleton(Placeholder.curlyTrusted("PLAYER", getPlayer().getName()))
        );
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
