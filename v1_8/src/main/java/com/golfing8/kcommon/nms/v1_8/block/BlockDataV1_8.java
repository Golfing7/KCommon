package com.golfing8.kcommon.nms.v1_8.block;

import com.golfing8.kcommon.nms.block.NMSBlockData;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.IBlockData;

/**
 * 1.8 NMS implementation for block data
 */
@AllArgsConstructor
public class BlockDataV1_8 implements NMSBlockData {
    private final IBlockData data;

    @Override
    public Object getHandle() {
        return data;
    }
}
