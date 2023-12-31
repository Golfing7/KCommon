package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.PotionData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public interface NMSMagicItems {
    EntityType getSpawnerType(ItemStack stack);

    int getRemainingItemDurability(ItemStack stack);

    boolean setRemainingItemDurability(ItemStack stack, int amount);

    void setUnbreakable(ItemMeta meta, boolean value);

    /**
     * Sets the base potion effect of the given potion.
     *
     * @param meta the meta.
     */
    default void setBaseEffect(PotionMeta meta, PotionData potionData) {
        meta.setBasePotionData(new org.bukkit.potion.PotionData(potionData.getPotionType(), potionData.isExtended(), potionData.isAmplified()));
    }

    /**
     * Gets the base potion effect of the given potion meta.
     *
     * @param meta the meta.
     * @return the potion data.
     */
    default PotionData getBaseEffect(PotionMeta meta) {
        org.bukkit.potion.PotionData potionData = meta.getBasePotionData();
        return new PotionData(potionData.getType(), potionData.isUpgraded(), potionData.isExtended());
    }

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
