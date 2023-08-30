package com.golfing8.kcommon.menu;

import lombok.Getter;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public final class MenuManager extends BukkitRunnable {

    @Getter
    private static MenuManager instance;

    private final List<Menu> allMenus;
    private BukkitTask managerTask;

    public MenuManager(Plugin plugin) {
        instance = this;

        this.managerTask = runTaskTimer(plugin, 0, 1);

        this.allMenus = new ArrayList<>();
    }

    /**
     * Gets all currently active menus on the server. Clones the backing list so modification
     * is impossible.
     *
     * @return the collection of all menus.
     */
    public Collection<Menu> getAll() {
        return Collections.unmodifiableCollection(allMenus);
    }

    public void addMenu(Menu menu) {
        this.allMenus.add(menu);
    }

    @Override
    public void run() {
        Iterator<Menu> menuIterator = allMenus.iterator();

        while (menuIterator.hasNext()) {
            Menu menu = menuIterator.next();

            if (menu.canExpire() && menu.getViewers().isEmpty()) {
                HandlerList.unregisterAll(menu);

                menu.shutdown();
                menuIterator.remove();
            }

            if (menu instanceof MenuDynamic) {
                ((MenuDynamic) menu).tickDynamics();
            }
        }
    }
}
