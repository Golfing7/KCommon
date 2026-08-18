package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.entity.Player;
import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages and garbage collects old menus
 */
public final class MenuManager {

    @Getter
    private static MenuManager instance;

    private final Map<UUID, Menu> allMenus;
    private final FoliaSchedulers schedulers;
    private final WrappedTask managerTask;

    public MenuManager(Plugin plugin) {
        instance = this;

        this.schedulers = FoliaSchedulers.of(plugin);
        this.managerTask = this.schedulers.runTimer(this::run, 0, 1);

        this.allMenus = new ConcurrentHashMap<>();
    }

    /**
     * Gets all currently active menus on the server. Clones the backing list so modification
     * is impossible.
     *
     * @return the collection of all menus.
     */
    public Collection<Menu> getAll() {
        return Collections.unmodifiableCollection(allMenus.values());
    }

    /**
     * Get the menu under the given id
     *
     * @param uuid the id
     * @return the menu
     */
    public @Nullable Menu getMenu(UUID uuid) {
        return this.allMenus.get(uuid);
    }

    /**
     * Add a menu to track
     *
     * @param menu the menu
     */
    public void addMenu(Menu menu) {
        this.allMenus.put(menu.getMenuID(), menu);
    }

    /**
     * Performs one garbage-collection tick over tracked menus.
     */
    public void run() {
        Iterator<Map.Entry<UUID, Menu>> menuIterator = allMenus.entrySet().iterator();

        while (menuIterator.hasNext()) {
            Menu menu = menuIterator.next().getValue();
            // If the menu was just created, let it go for a bit.
            if (menu.getCreatedTick() + 20 > NMS.getTheNMS().getCurrentTick())
                continue;

            // If the menu was manually shutdown, just remove it
            if (!menu.isValid()) {
                menuIterator.remove();
                continue;
            }

            if (menu.canExpire() && menu.getViewers().isEmpty()) {
                menu.shutdown();
                menuIterator.remove();
                continue;
            }

            List<Player> viewers = menu.getViewers();
            if (viewers.size() == 1) {
                Player viewer = viewers.get(0);
                this.schedulers.runAtEntityNow(viewer, () -> tickMenu(menu));
            } else {
                tickMenu(menu);
            }
        }
    }

    private void tickMenu(Menu menu) {
        menu.onTick();

        if (menu instanceof MenuDynamic) {
            ((MenuDynamic) menu).tickDynamics();
        }
    }
}
