package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicInventories;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import net.minecraft.server.v1_8_R3.ContainerAnvil;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryAnvil;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryView;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;

import java.util.List;

public class MagicInventoriesV1_8 implements NMSMagicInventories {
    @SuppressWarnings("unchecked")
    private final FieldHandle<String> nameHandle = (FieldHandle<String>) FieldHandles.getHandle("l", ContainerAnvil.class);

    @Override
    public String getAnvilRenameText(InventoryView view) {
        CraftInventoryView inventoryView = (CraftInventoryView) view;
        ContainerAnvil container = (ContainerAnvil) inventoryView.getHandle();
        return nameHandle.get(container);
    }
}
