package com.golfing8.kcommon.menu;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.player.pages.CustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * The Hytale page that shows a {@link PlayerMenuContainer}'s menu and runs its actions.
 */
final class MenuPage extends CustomUIPage {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final PlayerMenuContainer container;
    private @Nullable Menu pending;
    private @Nullable Menu current;

    MenuPage(PlayerRef player, PlayerMenuContainer container, Menu first) {
        super(player, first.getLifetime());
        this.container = container;
        this.pending = first;
    }

    @Override
    public void build(@NotNull Ref<EntityStore> ref, @NotNull UICommandBuilder commands, @NotNull UIEventBuilder events, @NotNull Store<EntityStore> store) {
        Menu menu = pending != null ? pending : container.loadMenu();
        pending = null;
        setLifetime(menu.getLifetime());
        menu.apply(commands, events);
        current = menu;
    }

    @Override
    public void handleDataEvent(@NotNull Ref<EntityStore> ref, @NotNull Store<EntityStore> store, @Nullable String rawData) {
        Menu menu = current;
        Map<String, String> values = parse(rawData);
        ClickAction action = menu == null ? null : menu.action(values.get(MenuBuilder.ACTION_KEY));
        if (action == null)
            return;

        try {
            action.onClick(new MenuClickEvent(container, ref, store, values));
        } catch (RuntimeException exc) {
            LOGGER.atSevere().withCause(exc).log("A menu action failed for %s", playerRef.getUsername());
        }
    }

    @Override
    public void onDismiss(@NotNull Ref<EntityStore> ref, @NotNull Store<EntityStore> store) {
        container.dismissed(this);
    }

    void refreshPage() {
        rebuild();
    }

    void closePage() {
        close();
    }

    private static Map<String, String> parse(@Nullable String rawData) {
        Map<String, String> values = new HashMap<>();
        if (rawData == null)
            return values;
        try {
            JsonElement element = JsonParser.parseString(rawData);
            if (!element.isJsonObject())
                return values;
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) element).entrySet()) {
                JsonElement value = entry.getValue();
                values.put(entry.getKey(), value.isJsonPrimitive() ? value.getAsString() : value.toString());
            }
        } catch (RuntimeException exc) {
            // Malformed client data; ignore the event.
            values.clear();
        }
        return values;
    }
}
