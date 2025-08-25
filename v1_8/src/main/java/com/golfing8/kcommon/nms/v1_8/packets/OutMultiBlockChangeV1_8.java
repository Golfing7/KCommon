package com.golfing8.kcommon.nms.v1_8.packets;

import com.golfing8.kcommon.nms.packets.NMSOutMultiBlockChange;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.Position;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_8_R3.PacketPlayOutMultiBlockChange;

import java.util.List;

@AllArgsConstructor
public class OutMultiBlockChangeV1_8 implements NMSOutMultiBlockChange {
    private static final FieldHandle<ChunkCoordIntPair> CC_FIELD =
            (FieldHandle<ChunkCoordIntPair>) FieldHandles.getHandle("a", PacketPlayOutMultiBlockChange.class);
    private static final FieldHandle<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]> BLOCK_CHANGE_INFO =
            (FieldHandle<PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[]>) FieldHandles.getHandle("b", PacketPlayOutMultiBlockChange.class);
    private static final int X_BITS = 0b1111000000000000;
    private static final int Z_BITS = 0b0000111100000000;
    private static final int Y_BITS = 0b0000000011111111;

    private final PacketPlayOutMultiBlockChange packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public List<Position> getPositions() {
        ChunkCoordIntPair cc = CC_FIELD.get(packet);

        PacketPlayOutMultiBlockChange.MultiBlockChangeInfo[] array = BLOCK_CHANGE_INFO.get(packet);

        List<Position> all = Lists.newArrayList();

        int blockX = cc.x << 4;
        int blockZ = cc.z << 4;

        for (PacketPlayOutMultiBlockChange.MultiBlockChangeInfo changeInfo : array) {
            int x = changeInfo.b() & X_BITS;
            int y = changeInfo.b() & Y_BITS;
            int z = changeInfo.b() & Z_BITS;

            all.add(new Position(x + blockX, y, z + blockZ));
        }
        return all;
    }
}
