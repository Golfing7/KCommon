package com.golfing8.kcommon.nms.v1_19.enchant;

import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * An implementation of the {@link NMSEnchant} for MC 1.19 R1.
 */
public class Enchant implements NMSEnchant {
    /**
     * The enchantment backing this instance.
     */
    private final Enchantment enchantment;

    public Enchant(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public Object getHandle() {
        return enchantment;
    }

    @Override
    public String getTranslatedName() {
        return IChatBaseComponent.c(enchantment.g()).getString();
    }
}
