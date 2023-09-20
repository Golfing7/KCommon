package com.golfing8.kcommon.nms.unknown.enchant;

import com.golfing8.kcommon.nms.enchant.NMSEnchant;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.enchantments.Enchantment;

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
        String content = ((TextComponent) enchantment.displayName(1)).content();
        return content.substring(0, content.length() - 2);
    }
}
