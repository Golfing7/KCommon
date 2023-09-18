package com.golfing8.kcommon.util;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import lombok.experimental.UtilityClass;
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
        if (meta.hasDisplayName())
            meta.setDisplayName(MS.parseSingle(meta.getDisplayName(), placeholders));

        if (!meta.hasLore()) {
            itemStack.setItemMeta(meta);
            return;
        }

        meta.setLore(MS.parseAll(meta.getLore(), placeholders));
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

        meta.setLore(MS.parseAllMulti(meta.getLore(), placeholders));
        itemStack.setItemMeta(meta);
    }
}
