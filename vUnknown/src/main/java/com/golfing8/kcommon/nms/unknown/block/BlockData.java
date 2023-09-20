package com.golfing8.kcommon.nms.unknown.block;

import com.golfing8.kcommon.nms.block.NMSBlockData;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BlockData implements NMSBlockData {
    private final org.bukkit.block.data.BlockData data;

    @Override
    public Object getHandle() {
        return data;
    }
}
