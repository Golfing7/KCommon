package com.golfing8.kcommon.nms.v1_17.enchant;

import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * An implementation of the {@link NMSEnchant} for MC 1.19 R1.
 */
public class Enchantv1_17 implements NMSEnchant {
    /**
     * The enchantment backing this instance.
     */
    private final Enchantment enchantment;

    public Enchantv1_17(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public Object getHandle() {
        return enchantment;
    }

    @Override
    public String getTranslatedName() {
        return IChatBaseComponent.a(enchantment.g()).getString();
    }
}
