package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicNumberAccess;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.v1_8.block.BlockV1_8;
import com.golfing8.kcommon.nms.v1_8.enchant.EnchantV1_8;
import com.golfing8.kcommon.nms.v1_8.item.ItemV1_8;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;

/**
 * NMS 1.8 number access
 */
public class MagicNumbersV1_8 implements NMSMagicNumberAccess {
    @Override
    public NMSItem getItem(Material material) {
        return new ItemV1_8(CraftMagicNumbers.getItem(material));
    }

    @Override
    public NMSBlock getBlock(Material material) {
        return new BlockV1_8(CraftMagicNumbers.getBlock(material));
    }

    @Override
    public NMSEnchant getEnchant(Enchantment enchantment) {
        return new EnchantV1_8(net.minecraft.server.v1_8_R3.Enchantment.getById(enchantment.getId()));
    }
}
