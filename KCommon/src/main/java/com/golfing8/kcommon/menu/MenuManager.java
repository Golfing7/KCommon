package com.golfing8.kcommon.menu;

import lombok.Getter;
import lombok.var;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class MenuManager extends BukkitRunnable {

    @Getter
    private static MenuManager instance;

    private final Map<UUID, Menu> allMenus;
    private BukkitTask managerTask;

    public MenuManager(Plugin plugin) {
        instance = this;

        this.managerTask = runTaskTimer(plugin, 0, 1);

        this.allMenus = new HashMap<>();
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

    public @Nullable Menu getMenu(UUID uuid) {
        return this.allMenus.get(uuid);
    }

    public void addMenu(Menu menu) {
        this.allMenus.put(menu.getMenuID(), menu);
    }

    @Override
    public void run() {
        var menuIterator = allMenus.entrySet().iterator();

        while (menuIterator.hasNext()) {
            Menu menu = menuIterator.next().getValue();
            // If the menu was manually shutdown, just remove it
            if (!menu.isValid()) {
                menuIterator.remove();
                continue;
            }

            if (menu.canExpire() && menu.getViewers().isEmpty()) {
                menu.shutdown();
                menuIterator.remove();
            }

            if (menu instanceof MenuDynamic) {
                ((MenuDynamic) menu).tickDynamics();
            }
        }
    }
}
