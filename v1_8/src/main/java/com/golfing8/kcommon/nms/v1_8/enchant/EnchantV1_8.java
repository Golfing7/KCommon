package com.golfing8.kcommon.nms.v1_8.enchant;

import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.LocaleI18n;

/**
 * An implementation of the {@link NMSEnchant} for MC 1.8
 */
public class EnchantV1_8 implements NMSEnchant {
    /**
     * The enchantment backing this instance.
     */
    private final Enchantment enchantment;

    public EnchantV1_8(Enchantment enchantment) {
        this.enchantment = enchantment;
    }

    @Override
    public Object getHandle() {
        return enchantment;
    }

    @Override
    public String getTranslatedName() {
        return LocaleI18n.get(enchantment.a());
    }
}
