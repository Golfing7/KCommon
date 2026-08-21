package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.movement.MoveLength;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CAMoveLengthTest {

    @Test
    @DisplayName("Round trips a move length through its slot list")
    void testRoundTrip() {
        MoveLength length = new MoveLength(Arrays.asList(new MenuCoordinate(0), new MenuCoordinate(9), new MenuCoordinate(18)));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(length);

        MoveLength loaded = ConfigTypeRegistry.getFromType(primitive, MoveLength.class);
        assertEquals(length.getCoordinates(), loaded.getCoordinates());
    }

    @Test
    @DisplayName("Parses a range-string as a vertical movement")
    void testParsesRangeString() {
        CAMoveLength adapter = new CAMoveLength();
        MoveLength loaded = adapter.toPOJO(ConfigPrimitive.ofString(new Range(0, 18).toString()), null);
        assertEquals(3, loaded.getCoordinates().size());
    }
}
