package com.golfing8.kcommon.nms.block;

import com.golfing8.kcommon.nms.NMSObject;

public interface NMSBlock extends NMSObject {

    NMSBlockData getBlockData();

    NMSBlockData fromLegacyData(int data);
}
