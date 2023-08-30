package com.golfing8.kcommon.nms.v1_19.access;

import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.access.NMSMagicNumberAccess;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import com.golfing8.kcommon.nms.v1_19.enchant.Enchant;
import com.golfing8.kcommon.nms.v1_19.block.Block;
import com.golfing8.kcommon.nms.v1_19.item.Item;
import net.minecraft.core.IRegistry;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftNamespacedKey;
import org.bukkit.enchantments.Enchantment;

public class MagicNumbers implements NMSMagicNumberAccess {
    @Override
    public NMSItem getItem(Material material) {
        return new Item(CraftMagicNumbers.getItem(material));
    }

    @Override
    public NMSBlock getBlock(Material material) {
        return new Block(CraftMagicNumbers.getBlock(material));
    }

    @Override
    public NMSEnchant getEnchant(Enchantment enchantment) {
        return new Enchant(IRegistry.W.a(CraftNamespacedKey.toMinecraft(enchantment.getKey())));
    }
}
