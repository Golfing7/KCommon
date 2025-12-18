package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicNumberAccess;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.unknown.block.Block;
import com.golfing8.kcommon.nms.unknown.enchant.Enchant;
import com.golfing8.kcommon.nms.unknown.item.Item;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * API agnostic number access
 */
public class MagicNumbers implements NMSMagicNumberAccess {
    @Override
    public NMSItem getItem(Material material) {
        return new Item(material);
    }

    @Override
    public NMSBlock getBlock(Material material) {
        return new Block(material.createBlockData());
    }

    @Override
    public NMSEnchant getEnchant(Enchantment enchantment) {
        return new Enchant(enchantment);
    }
}
