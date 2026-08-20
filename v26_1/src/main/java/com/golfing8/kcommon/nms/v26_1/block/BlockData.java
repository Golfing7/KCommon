package com.golfing8.kcommon.nms.v26_1.block;

import com.golfing8.kcommon.nms.block.NMSBlockData;
import lombok.AllArgsConstructor;

/**
 * Modern API binding for block data
 */
@AllArgsConstructor
public class BlockData implements NMSBlockData {
    private final org.bukkit.block.data.BlockData data;

    @Override
    public Object getHandle() {
        return data;
    }
}
