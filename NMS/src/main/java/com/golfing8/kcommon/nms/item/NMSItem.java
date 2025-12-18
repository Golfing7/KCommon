package com.golfing8.kcommon.nms.item;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.block.NMSBlock;

/**
 * NMS access for an item
 */
public interface NMSItem extends NMSObject {

    /**
     * Gets the destroy speed of the given stack on the given block
     *
     * @param itemStack the item
     * @param block the block
     * @return the destroy speed
     */
    float getDestroySpeed(NMSItemStack itemStack, NMSBlock block);

    /**
     * Creates a new item stack from this item with the given amount
     *
     * @param amount the amount
     * @return the item
     */
    NMSItemStack newStack(int amount);
}
