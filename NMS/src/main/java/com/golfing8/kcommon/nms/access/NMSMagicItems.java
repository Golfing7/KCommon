package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.Hand;
import com.golfing8.kcommon.nms.struct.PotionData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface NMSMagicItems {
    EntityType getSpawnerType(ItemStack stack);

    int getRemainingItemDurability(ItemStack stack);

    boolean setRemainingItemDurability(ItemStack stack, int amount);

    boolean isUnbreakable(ItemMeta meta);

    void setUnbreakable(ItemMeta meta, boolean value);

    void setAttributeModifiers(ItemStack meta, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers);

    void setExtraAttributeModifiers(ItemStack meta, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers);

    void setSkullOwningPlayer(SkullMeta meta, OfflinePlayer offlinePlayer);

    /**
     * Applies the name to the item and tries to use mini message if available.
     *
     * @param meta the meta.
     * @param name the name of the item.
     */
    void applyName(ItemMeta meta, @Nullable String name);

    /**
     * Applies the lore to the given item in mini message format if available.
     *
     * @param meta the item meta.
     * @param lore the new lore.
     */
    void applyLore(ItemMeta meta, @Nullable List<String> lore);

    /**
     * Gets a mini message serialized item name.
     *
     * @param meta the meta.
     * @return the mini message formatted display name.
     */
    String getMMDisplayName(ItemMeta meta);

    /**
     * Gets the mini message serialized lore.
     *
     * @param meta the meta of the item.
     * @return the mini message formatted lore.
     */
    List<String> getMMLore(ItemMeta meta);

    /**
     * Gets the displayed name of the given item.
     *
     * @return the displayed name.
     */
    String getDisplayName(ItemStack itemStack);

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

    default void setItemInHand(Player player, Hand hand, ItemStack stack) {
        switch (hand) {
            case MAIN:
                player.getInventory().setItemInMainHand(stack);
                return;
            case OFF_HAND:
                player.getInventory().setItemInOffHand(stack);
        }
    }

    default ItemStack getItemInHand(Player player, Hand hand) {
        switch (hand) {
            case MAIN:
                return player.getInventory().getItemInMainHand();
            case OFF_HAND:
                return player.getInventory().getItemInOffHand();
        }
        return null;
    }

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
