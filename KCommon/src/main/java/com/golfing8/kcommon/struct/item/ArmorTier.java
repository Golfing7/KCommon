package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XMaterial;

/**
 * Represents a tier of armor for a player.
 */
public enum ArmorTier {
    LEATHER,
    CHAIN,
    GOLD,
    IRON,
    DIAMOND,
    NETHERITE,
    /**
     * Used specifically for the turtle helmet.
     */
    TURTLE,
    ;

    /**
     * Reads the armor tier of the given material.
     *
     * @param mat the material.
     * @return the armor tier.
     */
    public static ArmorTier of(XMaterial mat) {
        // First verify that the material is actually armor.
        String matName = mat.name();
        if (!matName.contains("HELMET") && !matName.contains("CHESTPLATE") && !matName.contains("LEGGINGS") && !matName.contains("BOOTS"))
            return null;

        String[] split = matName.split("_");
        return ArmorTier.valueOf(split[0]);
    }
}
