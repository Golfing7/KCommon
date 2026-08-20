package com.golfing8.kcommon.nms.v26_1.server;

import com.golfing8.kcommon.nms.server.NMSServer;

/**
 * API agnostic server
 */
public class Server implements NMSServer {
    @Override
    public Object getHandle() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getSessionService() {
        throw new UnsupportedOperationException();
    }
}
