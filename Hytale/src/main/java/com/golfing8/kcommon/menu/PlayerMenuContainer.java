package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.util.HytalePlayers;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Owns a menu for one player, the Hytale counterpart of the Bukkit {@code PlayerMenuContainer}.
 * <p>
 * {@link #loadMenu()} builds the menu from scratch every time it is called, so {@link #refresh()}
 * shows changed state. Every method is safe to call from any thread; the work runs on the player's world thread.
 * </p>
 */
public abstract class PlayerMenuContainer {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    @Getter
    private final PlayerRef player;
    private @Nullable MenuPage page;

    /**
     * Creates a container for the player.
     *
     * @param player the player.
     */
    protected PlayerMenuContainer(PlayerRef player) {
        this.player = player;
    }

    /**
     * Builds the menu, typically by loading a {@link MenuBuilder} from config, binding it and calling {@link MenuBuilder#build()}.
     *
     * @return the menu.
     */
    protected abstract Menu loadMenu();

    /**
     * Called once the player closed the menu or another page replaced it.
     */
    protected void onClose() {
    }

    /**
     * Opens the menu, replacing any page the player has open.
     */
    public void open() {
        onWorld("open", ref -> {
            Store<EntityStore> store = ref.getStore();
            Player entity = store.getComponent(ref, Player.getComponentType());
            if (entity == null)
                return;
            MenuPage opened = new MenuPage(player, this, loadMenu());
            // Opening dismisses the page shown before, which may be this container's own.
            entity.getPageManager().openCustomPage(ref, store, opened);
            page = opened;
        });
    }

    /**
     * Rebuilds the open menu, so changed state and placeholders show. Does nothing if it isn't open.
     */
    public void refresh() {
        MenuPage opened = page;
        if (opened != null)
            onWorld("refresh", ref -> {
                if (page == opened)
                    opened.refreshPage();
            });
    }

    /**
     * Closes the menu if it is open.
     */
    public void close() {
        MenuPage opened = page;
        if (opened != null)
            onWorld("close", ref -> {
                if (page == opened)
                    opened.closePage();
            });
    }

    /**
     * Gets the player's entity component. Only call it on the player's world thread, as
     * {@link #loadMenu()} and click actions are.
     *
     * @return the player, or null while they have no valid entity.
     */
    protected @Nullable Player getEntity() {
        Ref<EntityStore> ref = player.getReference();
        if (ref == null || !ref.isValid())
            return null;
        return ref.getStore().getComponent(ref, Player.getComponentType());
    }

    /**
     * Checks if the menu is open.
     *
     * @return true if the player has it open.
     */
    public boolean isOpen() {
        return page != null;
    }

    void dismissed(MenuPage dismissed) {
        if (page != dismissed)
            return;
        page = null;
        onClose();
    }

    private void onWorld(String what, Consumer<Ref<EntityStore>> action) {
        HytalePlayers.runOnWorld(player, action).exceptionally(thr -> {
            LOGGER.atSevere().withCause(thr).log("Failed to %s a menu for %s", what, player.getUsername());
            return false;
        });
    }
}
