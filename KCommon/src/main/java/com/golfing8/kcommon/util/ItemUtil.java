package com.golfing8.kcommon.util;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Utilities for {@link ItemStack} objects.
 */
@UtilityClass
public final class ItemUtil {

    /**
     * Gets the display name of the itemstack.
     *
     * @param itemStack the itemstack.
     * @return its displayed name
     */
    public static String getDisplayName(ItemStack itemStack) {
        if (itemStack == null)
            return "Air";

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        } else {
            NMSItemStack stack = NMS.getTheNMS().getMagicItems().wrapItemStack(itemStack);
            return stack.getI18DisplayName();
        }
    }

    /**
     * Applies the placeholders to the given item.
     *
     * @param itemStack the item
     * @param placeholders the placeholders
     */
    public static void applyPlaceholders(ItemStack itemStack, Collection<Placeholder> placeholders) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasDisplayName()) {
            String displayName = NMS.getTheNMS().getMagicItems().getMMDisplayName(meta);
            NMS.getTheNMS().getMagicItems().applyName(meta, MS.parseSingle(displayName, placeholders));
        }

        if (!meta.hasLore()) {
            itemStack.setItemMeta(meta);
            return;
        }

        List<String> lore = NMS.getTheNMS().getMagicItems().getMMLore(meta);
        NMS.getTheNMS().getMagicItems().applyLore(meta, MS.parseAll(lore, placeholders));
        itemStack.setItemMeta(meta);
    }

    /**
     * Applies the multi-line placeholders to the given item.
     *
     * @param itemStack the item.
     * @param placeholders the placeholders.
     */
    public static void applyMPlaceholders(ItemStack itemStack, Collection<MultiLinePlaceholder> placeholders) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.hasLore()) {
            return;
        }

        List<String> lore = NMS.getTheNMS().getMagicItems().getMMLore(meta);
        NMS.getTheNMS().getMagicItems().applyLore(meta, MS.parseAllMulti(lore, placeholders));
        itemStack.setItemMeta(meta);
    }

    /**
     * Checks if the item is air or null or has an amount of 0.
     *
     * @param check the check.
     * @return true if the item is air or null.
     */
    public static boolean isAirOrNull(ItemStack check){
        return check == null || check.getType() == XMaterial.AIR.parseMaterial() || check.getAmount() <= 0;
    }
}
