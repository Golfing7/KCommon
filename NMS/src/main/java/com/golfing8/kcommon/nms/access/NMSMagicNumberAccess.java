package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

public interface NMSMagicNumberAccess {

    NMSItem getItem(Material material);

    NMSBlock getBlock(Material material);

    NMSEnchant getEnchant(Enchantment enchantment);
}
