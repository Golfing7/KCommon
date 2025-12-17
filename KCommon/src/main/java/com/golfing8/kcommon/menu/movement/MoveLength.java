package com.golfing8.kcommon.menu.movement;

import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * A series of coordinates that an item can move through
 */
@Getter
@AllArgsConstructor
public class MoveLength {
    private final List<MenuCoordinate> coordinates;
}
