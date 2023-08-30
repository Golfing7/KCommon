package com.golfing8.kcommon.nms.enchant;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * Represents an NMS enchantment.
 */
public interface NMSEnchant extends NMSObject {

    /**
     * Gets the locale name for the enchantment.
     * @return the locale name.
     */
    String getTranslatedName();
}
