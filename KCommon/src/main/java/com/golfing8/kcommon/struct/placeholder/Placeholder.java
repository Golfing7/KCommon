package com.golfing8.kcommon.struct.placeholder;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * A placeholder for a given string. Can be used in messages or things like items. KCommon's general
 * convention for placeholders is {BRACKETS_UPPER_CASE}.
 */
@AllArgsConstructor
@EqualsAndHashCode
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

    public Placeholder(Placeholder placeholder) {
        this.label = placeholder.label;
        this.value = placeholder.value;
    }

    @Override
    public String toString() {
        return value;
    }

    /**
     * Generates a placeholder from a list with the default convention for naming.
     * <br>
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
     * Compiles an argument list of placeholders of the format: "LABEL-1" -> value1, "LABEL-2" -> value2, etc.
     * <p>
     * If any of the given arguments are already instances of {@link Placeholder}, their label and value are pulled and
     * treated as if they were supplied separately.
     * </p>
     * Null values are not permitted.
     *
     * @param objects the objects.
     * @return the list of parsed placeholders.
     */
    public static List<Placeholder> compileCurly(Object... objects) {
        List<Placeholder> placeholders = new ArrayList<>();
        int index = 0;
        while (index < objects.length) {
            Object object = objects[index];
            if (object instanceof Placeholder) {
                placeholders.add(new Placeholder((Placeholder) object));
                index++;
            } else {
                if (index + 1 >= objects.length)
                    throw new IllegalArgumentException("Unbalanced placeholder list: " + Arrays.toString(objects));

                String key = "{" + objects[index] + "}";
                String value = objects[index + 1].toString();
                placeholders.add(new Placeholder(key, value));
                index += 2;
            }
        }
        return placeholders;
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

    /**
     * Generates a placeholder with the default convention for naming.
     *
     * @param label the label, which is surrounded with {} and uppercased.
     * @param value the value to replace it with.
     * @return the placeholder generated.
     */
    public static Placeholder curly(@Nonnull String label, @Nonnull Object value) {
        String trueLabel = "{" + label.toUpperCase() + "}";
        return new Placeholder(trueLabel, value.toString());
    }
}
