package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.access.NMSMagicNumberAccess;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.v1_17.block.BlockV1_17;
import com.golfing8.kcommon.nms.v1_17.enchant.Enchantv1_17;
import com.golfing8.kcommon.nms.v1_17.item.ItemV1_17;
import net.minecraft.core.IRegistry;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class MagicNumbersV1_17 implements NMSMagicNumberAccess {
    @Override
    public NMSItem getItem(Material material) {
        return new ItemV1_17(CraftMagicNumbers.getItem(material));
    }

    @Override
    public NMSBlock getBlock(Material material) {
        return new BlockV1_17(CraftMagicNumbers.getBlock(material));
    }

    @Override
    public NMSEnchant getEnchant(Enchantment enchantment) {
        return new Enchantv1_17(IRegistry.X.get(CraftNamespacedKey.toMinecraft(enchantment.getKey())));
    }
}
