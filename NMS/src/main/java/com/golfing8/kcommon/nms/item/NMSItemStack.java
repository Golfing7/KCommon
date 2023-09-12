package com.golfing8.kcommon.nms.item;

import com.golfing8.kcommon.nms.NMSObject;

public interface NMSItemStack extends NMSObject {
    /**
     * Gets the MC translated display name.
     *
     * @return the display name.
     */
    String getI18DisplayName();
}
