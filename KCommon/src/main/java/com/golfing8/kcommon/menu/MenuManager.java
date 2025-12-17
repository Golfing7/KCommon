package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.NMS;
import lombok.Getter;
import lombok.var;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages and garbage collects old menus
 */
public final class MenuManager extends BukkitRunnable {

    @Getter
    private static MenuManager instance;

    private final Map<UUID, Menu> allMenus;
    private final BukkitTask managerTask;

    public MenuManager(Plugin plugin) {
        instance = this;

        this.managerTask = runTaskTimer(plugin, 0, 1);

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

    @Override
    public void run() {
        var menuIterator = allMenus.entrySet().iterator();

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

            menu.onTick();

            if (menu instanceof MenuDynamic) {
                ((MenuDynamic) menu).tickDynamics();
            }
        }
    }
}
