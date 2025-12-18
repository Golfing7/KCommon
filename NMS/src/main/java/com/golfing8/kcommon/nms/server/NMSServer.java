package com.golfing8.kcommon.nms.server;

import com.golfing8.kcommon.nms.NMSObject;

/**
 * NMS access for the server
 */
public interface NMSServer extends NMSObject {
    /**
     * Gets the session service object
     *
     * @return the session service
     */
    Object getSessionService();
}
