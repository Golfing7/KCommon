package com.golfing8.kcommon.nms.item;

import com.golfing8.kcommon.nms.NMSObject;
import com.golfing8.kcommon.nms.block.NMSBlock;

public interface NMSItem extends NMSObject {

    float getDestroySpeed(NMSItemStack itemStack, NMSBlock block);

    NMSItemStack newStack(int amount);
}
