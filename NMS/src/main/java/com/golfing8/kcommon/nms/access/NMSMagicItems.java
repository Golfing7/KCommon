package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public interface NMSMagicItems {
    EntityType getSpawnerType(ItemStack stack);

    int getRemainingItemDurability(ItemStack stack);

    boolean setRemainingItemDurability(ItemStack stack, int amount);

    void setUnbreakable(ItemMeta meta, boolean value);

    /**
     * Sets the custom model data for the given item meta.
     *
     * @param meta the item meta.
     * @param modelData the custom model data.
     */
    void setCustomModelData(ItemMeta meta, int modelData);

    /**
     * Wraps the given item stack.
     *
     * @param itemStack the item stack.
     * @return the wrapped item stack.
     */
    NMSItemStack wrapItemStack(ItemStack itemStack);

    default void setUnbreakable(ItemStack itemStack, boolean value){
        if(itemStack == null || !itemStack.hasItemMeta())
            return;

        setUnbreakable(itemStack.getItemMeta(), value);
    }
}
