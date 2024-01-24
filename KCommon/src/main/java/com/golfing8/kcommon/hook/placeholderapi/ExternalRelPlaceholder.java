package com.golfing8.kcommon.hook.placeholderapi;

import lombok.Getter;
import org.bukkit.entity.Player;

/**
 * Represents a singular relational external placeholder and its implementation.
 */
@Getter
public class ExternalRelPlaceholder extends ExternalPlaceholder{
    public ExternalRelPlaceholder(String label, String description) {
        super(label, description);
    }
}
