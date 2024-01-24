package com.golfing8.kcommon.hook.placeholderapi;

import com.golfing8.kcommon.module.Module;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Represents a singular external placeholder and its implementation.
 */
@Getter
public class KPlaceholderDefinition implements Comparable<KPlaceholderDefinition> {
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

    public KPlaceholderDefinition(String label, String description) {
        this.label = label.toLowerCase();
        this.splitLabel = label.split("_");
        this.description = description;
    }

    /**
     * Formats the label to reflect the ENTIRE placeholder.
     *
     * @param module the module to format it on.
     * @return the formatted label.
     */
    public String formatLabel(Module module) {
        return module.getPlugin().getName().toLowerCase() + "_" + module.getModuleName() + "_" + this.label;
    }

    /**
     * Gets the amount of left-right matches of the parameters.
     *
     * @param parameters the parameters.
     * @return the amount that match.
     */
    public int getMatchCount(String[] parameters) {
        int i = 0;
        while (i < parameters.length && i < splitLabel.length && parameters[i].equalsIgnoreCase(splitLabel[i])) {
            i++;
        }
        return i;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        KPlaceholderDefinition that = (KPlaceholderDefinition) object;
        return Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label);
    }

    @Override
    public int compareTo(@NotNull KPlaceholderDefinition o) {
        return this.label.compareTo(o.label);
    }
}
