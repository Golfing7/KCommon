package com.golfing8.kcommon.struct.filter;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.regex.Pattern;

/**
 * A filter applied to java Strings.
 */
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StringFilter implements Filter<String> {
    /**
     * If case should be ignored
     */
    private boolean ignoreCase;
    /**
     * If the pattern should use regex
     */
    private boolean regex;
    /**
     * If we should only check for contains
     */
    private boolean contains;
    /**
     * The pattern to match
     */
    private String pattern;
    /**
     * Only set if {@link #regex} is true
     */
    private transient Pattern regexPattern;

    public StringFilter(String pattern, boolean ignoreCase, boolean contains, boolean regex) {
        this.pattern = pattern;
        this.ignoreCase = ignoreCase;
        this.contains = contains;
        this.regex = regex;
    }

    public StringFilter(String pattern) {
        this(pattern, false, false, false);
    }

    /**
     * Checks if this filter is simple, that being {@code ignoreCase, regex, contains} are all false.
     *
     * @return true if simple, false if not
     */
    public boolean isSimple() {
        return !ignoreCase && !regex && !contains;
    }

    @Override
    public int filter(String s) {
        if ((s == null) && (pattern == null))
            return 0;

        if (s == null || pattern == null)
            return 0;

        if (regex) {
            if (regexPattern == null)
                regexPattern = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);

            return (contains ? regexPattern.matcher(s).find() : regexPattern.matcher(s).matches()) ? 1 : 0;
        }

        if (ignoreCase) {
            return (contains ? s.toLowerCase().contains(pattern.toLowerCase()) : s.equals(pattern)) ? 1 : 0;
        }
        return (contains ? s.contains(pattern) : s.equals(pattern)) ? 1 : 0;
    }
}
