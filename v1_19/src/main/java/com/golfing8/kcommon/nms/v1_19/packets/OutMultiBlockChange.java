package com.golfing8.kcommon.nms.v1_19.packets;

import com.golfing8.kcommon.nms.packets.NMSOutMultiBlockChange;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.Position;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import net.minecraft.core.SectionPosition;
import net.minecraft.network.protocol.game.PacketPlayOutMultiBlockChange;

import java.util.List;

@AllArgsConstructor
public class OutMultiBlockChange implements NMSOutMultiBlockChange {
    private static final FieldHandle<SectionPosition> CC_FIELD =
            (FieldHandle<SectionPosition>) FieldHandles.getHandle("b", PacketPlayOutMultiBlockChange.class);
    private static final FieldHandle<short[]> BLOCK_CHANGE_INFO =
            (FieldHandle<short[]>) FieldHandles.getHandle("c", PacketPlayOutMultiBlockChange.class);
    private static final int X_BITS = 0b111100000000;
    private static final int Z_BITS = 0b000011110000;
    private static final int Y_BITS = 0b000000001111;

    private final PacketPlayOutMultiBlockChange packet;

    @Override
    public Object getHandle() {
        return packet;
    }

    @Override
    public List<Position> getPositions() {
        SectionPosition sp = CC_FIELD.get(packet);

        short[] array = BLOCK_CHANGE_INFO.get(packet);

        List<Position> all = Lists.newArrayList();

        int blockX = sp.u() << 4;
        int blockZ = sp.v() << 4;
        int blockY = sp.w() << 4;

        for(short changeInfo : array){
            int x = changeInfo & X_BITS;
            int y = changeInfo & Y_BITS;
            int z = changeInfo & Z_BITS;

            all.add(new Position(x + blockX, y + blockY, z + blockZ));
        }
        return all;
    }
}
