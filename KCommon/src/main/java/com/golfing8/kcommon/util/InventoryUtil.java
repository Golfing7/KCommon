package com.golfing8.kcommon.util;

import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiPredicate;
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
     * Updates the items in the inventory given the function
     *
     * @param inventory the inventory
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
}
