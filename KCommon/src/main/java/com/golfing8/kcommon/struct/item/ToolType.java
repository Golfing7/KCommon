package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XMaterial;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

/**
 * Represents a type of tool that a player uses.
 * Not all tool types have been enumerated. If one is needed, add it.
 */
@AllArgsConstructor
public enum ToolType {
    SWORD(material -> material.name().endsWith("SWORD")),
    AXE(material -> material.name().endsWith("_AXE")),
    PICKAXE(material -> material.name().endsWith("PICKAXE")),
    SHOVEL(material -> material.name().endsWith("SHOVEL")),
    HOE(material -> material.name().endsWith("_HOE")),
    FISHING_ROD(material -> material == XMaterial.FISHING_ROD),
    BOW(material -> material == XMaterial.BOW || material == XMaterial.CROSSBOW),
    ;

    final Predicate<XMaterial> sameTypePredicate;

    /**
     * Checks if the given item is in the same category as this
     *
     * @param itemStack the item
     * @return true if same category
     */
    public boolean isSameType(ItemStack itemStack) {
        return isSameType(XMaterial.matchXMaterial(itemStack));
    }

    /**
     * Checks if the given material is in the same category as this
     *
     * @param material the material
     * @return true if same category
     */
    public boolean isSameType(XMaterial material) {
        return sameTypePredicate.test(material);
    }
}
