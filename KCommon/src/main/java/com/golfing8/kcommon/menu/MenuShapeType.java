package com.golfing8.kcommon.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryType;

/**
 * Different types of inventories and their widths
 */
@Getter
@AllArgsConstructor
public enum MenuShapeType {
    CHEST(InventoryType.CHEST, 9, 6, true),
    DISPENSER(InventoryType.DISPENSER, 3, 3, false),
    HOPPER(InventoryType.HOPPER, 5, 1, false),
    ;

    private final InventoryType type;
    private final int width, maxRows;
    private final boolean sizeMutable;
}
