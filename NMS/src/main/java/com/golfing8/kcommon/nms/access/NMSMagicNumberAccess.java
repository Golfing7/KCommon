package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import com.golfing8.kcommon.nms.item.NMSItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * NMS access for magic numbers
 */
public interface NMSMagicNumberAccess {

    /**
     * Gets the item associated with the material
     *
     * @param material the material
     * @return the item
     */
    NMSItem getItem(Material material);

    /**
     * Gets the block associated with the material
     *
     * @param material the material
     * @return the block
     */
    NMSBlock getBlock(Material material);

    /**
     * Gets the enchantment associated with the bukkit enchant
     *
     * @param enchantment the enchantment
     * @return the NMS enchantment
     */
    NMSEnchant getEnchant(Enchantment enchantment);
}
