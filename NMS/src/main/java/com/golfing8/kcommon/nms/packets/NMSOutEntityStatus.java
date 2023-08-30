package com.golfing8.kcommon.nms.packets;

import com.golfing8.kcommon.nms.world.NMSWorld;
import org.bukkit.entity.Entity;

public interface NMSOutEntityStatus extends NMSPacket{
    Entity getEntity(NMSWorld world);

    int getEntityID();

    byte getCode();
}
