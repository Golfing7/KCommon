package com.golfing8.kcommon.menu;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A player activating an element of a menu. Runs on the player's world thread, so their components are safe to use.
 */
@Getter
public final class MenuClickEvent {
    private final PlayerMenuContainer container;
    private final Ref<EntityStore> ref;
    private final Store<EntityStore> store;
    /** Everything the client sent with the event. Never trust it. */
    private final Map<String, String> values;

    MenuClickEvent(PlayerMenuContainer container, Ref<EntityStore> ref, Store<EntityStore> store, Map<String, String> values) {
        this.container = container;
        this.ref = ref;
        this.store = store;
        this.values = values;
    }

    /**
     * Gets the player who clicked.
     *
     * @return the player reference.
     */
    public PlayerRef getPlayerRef() {
        return container.getPlayer();
    }

    /**
     * Gets the clicking player's entity component.
     *
     * @return the player, or null if the entity no longer has one.
     */
    public @Nullable Player getPlayer() {
        return store.getComponent(ref, Player.getComponentType());
    }

    /**
     * Gets the value of the bound input element, such as the text of a {@code TextField}.
     *
     * @return the value, or null if the element has none.
     */
    public @Nullable String getValue() {
        return values.get(MenuBuilder.VALUE_KEY);
    }

    /**
     * Rebuilds the menu, so changed state and placeholders show.
     */
    public void refresh() {
        container.refresh();
    }

    /**
     * Closes the menu.
     */
    public void close() {
        container.close();
    }
}
