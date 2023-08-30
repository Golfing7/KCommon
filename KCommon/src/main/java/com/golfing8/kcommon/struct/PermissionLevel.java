package com.golfing8.kcommon.struct;

import lombok.Data;

/**
 * Represents a permission level.
 */
@Data
public class PermissionLevel {
    private final double permissionLevel;
    private final String permissionLabel;
    private final String description;
}
