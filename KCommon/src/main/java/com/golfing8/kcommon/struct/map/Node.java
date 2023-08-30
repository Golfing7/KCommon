package com.golfing8.kcommon.struct.map;

import com.golfing8.kcommon.nms.struct.Position;

class Node {
    Node next;
    Node previous;
    Object value;
    Position key;

    Node(Position key, Object value)
    {
        this.key = key;
        this.value = value;
    }
}