package com.golfing8.kcommon.util;

import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public final class InventoryUtil {
    /**
     * Counts all items that match the given predicate in the inventory
     *
     * @param inventory the inventory
     * @param predicate the predicate of the item
     * @return the item count
     */
    public static int countItems(Inventory inventory, Predicate<@NotNull ItemStack> predicate) {
        int count = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() == XMaterial.AIR.parseMaterial() || itemStack.getAmount() <= 0)
                continue;

            if (predicate.test(itemStack)) {
                count += itemStack.getAmount();
            }
        }
        return count;
    }

    /**
     * Gets the amount of empty slots in the given inventory
     *
     * @param inventory the inventory
     * @return the amount of empty slots.
     */
    public static int countEmptySlots(Inventory inventory) {
        return countEmptySlots(inventory.getContents());
    }

    /**
     * Gets the amount of empty slots in the item array
     *
     * @param items the items
     * @return the amount of empty slots.
     */
    public static int countEmptySlots(ItemStack[] items) {
        int count = 0;
        for (ItemStack itemStack : items) {
            if (itemStack == null || itemStack.getType() == XMaterial.AIR.parseMaterial())
                count++;
        }
        return count;
    }

    /**
     * Updates the items in the inventory given the function
     *
     * @param inventory    the inventory
     * @param itemFunction the item function
     * @return the amount of updated items
     */
    public static int updateItems(Inventory inventory, Function<@NotNull ItemStack, @Nullable ItemStack> itemFunction) {
        int count = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType() == XMaterial.AIR.parseMaterial() || itemStack.getAmount() <= 0)
                continue;

            contents[i] = itemFunction.apply(itemStack);
        }
        inventory.setContents(contents);
        return count;
    }

    /**
     * Removes up to the given amount of items from the given inventory
     *
     * @param inventory the inventory
     * @param amount    the amount of items to remove
     * @param predicate the item predicate
     * @return the actual amount of items removed.
     */
    public static int removeUpTo(Inventory inventory, int amount, Predicate<@NotNull ItemStack> predicate) {
        int count = 0;
        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType() == XMaterial.AIR.parseMaterial() || itemStack.getAmount() <= 0)
                continue;

            int toRemove = Math.min(itemStack.getAmount(), amount - count);
            if (!predicate.test(itemStack))
                continue;

            if (toRemove >= itemStack.getAmount()) {
                contents[i] = null;
            } else {
                itemStack.setAmount(itemStack.getAmount() - toRemove);
            }
            count += toRemove;
        }
        inventory.setContents(contents);
        return count;
    }
}
