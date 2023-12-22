package com.golfing8.kcommon.menu;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * A data class for storing the shape of an inventory.
 */
@Getter
@AllArgsConstructor
public class MenuShape {
    private final MenuShapeType type;
    private final int size;
}
