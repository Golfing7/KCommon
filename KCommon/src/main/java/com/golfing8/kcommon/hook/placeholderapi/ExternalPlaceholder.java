package com.golfing8.kcommon.hook.placeholderapi;

import lombok.Getter;

/**
 * Represents a singular external placeholder and its implementation.
 */
@Getter
public class ExternalPlaceholder {
    /**
     * The label of a placeholder is what identifies it among others.
     */
    private final String label;
    /**
     * The label split by underscores.
     */
    private final String[] splitLabel;
    /**
     * The description of the placeholder.
     */
    private final String description;

    public ExternalPlaceholder(String label, String description) {
        this.label = label;
        this.splitLabel = label.split("_");
        this.description = description;
    }
}
