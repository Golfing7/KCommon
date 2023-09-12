package com.golfing8.kcommon.util;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;

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
}
