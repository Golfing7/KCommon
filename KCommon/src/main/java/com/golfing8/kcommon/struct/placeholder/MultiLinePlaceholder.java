package com.golfing8.kcommon.struct.placeholder;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.util.List;

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
 *
 */
@AllArgsConstructor
public class MultiLinePlaceholder {
    /**
     * The label of the placeholder, typically in %FORMAT%.
     */
    @Getter
    private final String label;
    /**
     * The replacement list to replace the label with.
     */
    @Getter
    private final List<String> replacement;

    /**
     * Generates a placeholder with the default naming convention and a list of string values.
     *
     * @param label the label, will be converted to uppercase and surrounded in '%'s
     * @param values the values to replace the label with.
     * @return the created placeholder.
     */
    public static MultiLinePlaceholder percent(@Nonnull String label, @Nonnull List<String> values) {
        String trueLabel = "%" + label.toUpperCase() + "%";
        return new MultiLinePlaceholder(trueLabel, values);
    }
}
