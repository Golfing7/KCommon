package com.golfing8.kcommon.nms.v1_8.server;

import com.golfing8.kcommon.nms.server.NMSServer;
import net.minecraft.server.v1_8_R3.MinecraftServer;

public class ServerV1_8 implements NMSServer {
    @Override
    public Object getHandle() {
        return MinecraftServer.getServer();
    }

    @Override
    public Object getSessionService() {
        return MinecraftServer.getServer().aD();
    }
}
