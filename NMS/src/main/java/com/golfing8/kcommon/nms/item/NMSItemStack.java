package com.golfing8.kcommon.nms.item;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * NMS access for a specific item stack
 */
public interface NMSItemStack extends NMSObject {
    /**
     * Gets the MC translated display name.
     *
     * @return the display name.
     */
    String getI18DisplayName();
}
