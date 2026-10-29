package com.golfing8.kcommon.struct.placeholder;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Used specifically in cases where multiple lines are replaced.
 * <br>
 * An item with text "something %PLACEHOLDER%" in its lore will do the following
 * <br>
 * - "Some text"<br>
 * - "Something Something" <br>
 * - "something %PLACEHOLDER%" <br>
 * and the replacement of lines counting 1-3 would be:
 * <br>
 * - "Some text"<br>
 * - "Something Something" <br>
 * - "1" <br>
 * - "2" <br>
 * - "3" <br>
 * Note that the line with the placeholder is deleted and replaced with the first line of the placeholder. This is true with messages as well.
 */
@Getter
public class MultiLinePlaceholder extends PlaceholderAbstract<List<String>, List<String>> {
    /**
     * The label of the placeholder, typically in %FORMAT%.
     */
    private final String label;
    /**
     * The replacement list to replace the label with.
     */
    private final List<String> replacement;

    public MultiLinePlaceholder(String label, List<?> replacement) {
        super(false);

        this.label = label;
        this.replacement = replacement.stream().map(Objects::toString).collect(Collectors.toList());
    }

    public MultiLinePlaceholder(String label, List<?> replacement, boolean trusted) {
        super(trusted);

        this.label = label;
        this.replacement = replacement.stream().map(Objects::toString).collect(Collectors.toList());
    }

    @Override
    public List<String> apply(List<String> in) {
        List<String> toReturn = new ArrayList<>(in);

        // Loop over all the messages and parse them one at a time
        for (int i = 0; i < toReturn.size(); i++) {
            String line = toReturn.get(i);

            if (!line.contains(this.getLabel()))
                continue;

            // Get the replacements and check if its empty
            List<String> replacement = this.getReplacement();
            if (replacement.isEmpty()) {
                toReturn.remove(i--);
                continue;
            }

            // Then start replacing them.
            toReturn.set(i, line.replace(this.getLabel(), Objects.toString(replacement.get(0))));
            for (int j = 1; j < replacement.size(); j++) {
                toReturn.add(i + j, line.replace(this.getLabel(), Objects.toString(replacement.get(j))));
            }
            break;
        }
        return toReturn;
    }

    /**
     * Generates a placeholder with the default naming convention and a list of string values.
     *
     * @param label  the label, will be converted to uppercase and surrounded in '%'s
     * @param values the values to replace the label with.
     * @return the created placeholder.
     * @deprecated Use {@link #percentTrusted(String, List)}
     */
    @Deprecated
    public static MultiLinePlaceholder percent(@NotNull String label, @NotNull List<?> values) {
        String trueLabel = "%" + label.toUpperCase() + "%";
        return new MultiLinePlaceholder(trueLabel, values, true);
    }

    /**
     * Generates a placeholder with the default naming convention and a list of string values.
     *
     * @param label  the label, will be converted to uppercase and surrounded in '%'s
     * @param values the values to replace the label with.
     * @return the created placeholder.
     */
    public static MultiLinePlaceholder percentTrusted(@NotNull String label, @NotNull List<?> values) {
        return percentTrustedArg(label, values, true);
    }

    /**
     * Generates a placeholder with the default naming convention and a list of string values.
     *
     * @param label  the label, will be converted to uppercase and surrounded in '%'s
     * @param values the values to replace the label with.
     * @return the created placeholder.
     */
    public static MultiLinePlaceholder percentUntrusted(@NotNull String label, @NotNull List<?> values) {
        return percentTrustedArg(label, values, false);
    }

    /**
     * Generates a placeholder with the default naming convention and a list of string values.
     *
     * @param label   the label, will be converted to uppercase and surrounded in '%'s
     * @param values  the values to replace the label with.
     * @param trusted if the placeholder is trusted.
     * @return the created placeholder.
     */
    public static MultiLinePlaceholder percentTrustedArg(@NotNull String label, @NotNull List<?> values, boolean trusted) {
        String trueLabel = "%" + label.toUpperCase() + "%";
        return new MultiLinePlaceholder(trueLabel, values, trusted);
    }
}
