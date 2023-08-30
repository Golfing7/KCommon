package com.golfing8.kcommon.struct.placeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A placeholder for a given string. Can be used in messages or things like items. KCommon's general
 * convention for placeholders is {BRACKETS_UPPER_CASE}.
 */
@AllArgsConstructor
public class Placeholder {
    /**
     * The label to replace, typically in {BRACKETS_UPPER_CASE} format.
     */
    @Getter
    private final String label;
    /**
     * The value to replace the label with.
     */
    @Getter
    private final String value;

    /**
     * Generates a placeholder from a list with the default convention for naming.
     * <p />
     * Uses the <code>valueFormat</code> for formatting values. If null, simply uses the default string from the list.
     *
     * @param label the label, which is surrounded with {} and uppercased.
     * @param value the value to replace it with.
     * @param delimiter the delimiter to place between each value in the list.
     * @param valueFormat the format for values.
     * @return the placeholder generated.
     */
    public static Placeholder formatList(@Nonnull String label,
                                         @Nonnull List<String> value,
                                         @Nullable String delimiter,
                                         @Nullable String valueFormat) {
        String trueLabel = "{" + label.toUpperCase() + "}";
        String trueFormat = valueFormat != null ? valueFormat : "%s";
        String trueDelimiter = delimiter != null ? delimiter : ", ";

        //Format the list.
        StringBuilder listBuilder = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            listBuilder.append(String.format(trueFormat, value.get(i)));

            //Append a comma if necessary.
            if(i + 1 != value.size()) {
                listBuilder.append(trueDelimiter);
            }
        }

        return new Placeholder(trueLabel, listBuilder.toString());
    }

    /**
     * Generates a placeholder with the default convention for naming.
     *
     * @param label the label, which is surrounded with {} and uppercased.
     * @param value the value to replace it with.
     * @return the placeholder generated.
     */
    public static Placeholder curly(@Nonnull String label, @Nonnull String value) {
        String trueLabel = "{" + label.toUpperCase() + "}";
        return new Placeholder(trueLabel, value);
    }
}
