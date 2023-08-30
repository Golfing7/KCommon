package com.golfing8.kcommon.nms.v1_17.server;

import com.golfing8.kcommon.nms.server.NMSServer;
import net.minecraft.server.MinecraftServer;

public class ServerV1_17 implements NMSServer {
    @Override
    public Object getHandle() {
        return MinecraftServer.getServer();
    }

    @Override
    public Object getSessionService() {
        return MinecraftServer.getServer().getMinecraftSessionService();
    }
}
