package com.golfing8.kcommon.nms.packets;

import org.bukkit.World;
import org.bukkit.entity.Entity;

public interface NMSInUseEntity extends NMSPacket {
    Entity getInteractedEntity(World world);
}
