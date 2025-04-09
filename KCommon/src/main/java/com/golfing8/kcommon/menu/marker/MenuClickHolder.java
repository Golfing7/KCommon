package com.golfing8.kcommon.menu.marker;

import com.golfing8.kcommon.menu.MenuAbstract;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

@AllArgsConstructor @Getter
public class MenuClickHolder implements InventoryHolder {
    private boolean clickable;
    private MenuAbstract menu;
    @Override
    public Inventory getInventory() {
        return null;
    }
}
