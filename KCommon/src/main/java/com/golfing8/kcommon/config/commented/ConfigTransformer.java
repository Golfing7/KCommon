package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A utility class for recognizing config keys and allowing iteration over them.
 */
public class ConfigTransformer implements Iterator<String>, Iterable<String> {
    /** The index of the last key in the transformed lines list. */
    private int lastKeyIndex = -1;
    /** The list of new lines to put into the config */
    @Getter
    private List<String> transformedLines = new ArrayList<>();
    /** The amount of lines that have been artificially inserted */
    private int linesInserted = 0;
    /** Lines, up to the current key, that weren't keys. */
    @Getter
    private List<String> junk = new ArrayList<>(), junk2 = new ArrayList<>();
    /** The index of the lines */
    private int index = 0;
    /** The indent of the lines */
    private int indent = -1, lastIndent = -1;
    /** The lines of the config */
    private String[] lines;
    /** The list of keys making up the path of the current key */
    private List<String> keys = new ArrayList<>();
    /** The key to return on the next {@link #next()} call. */
    private String nextKey;

    public ConfigTransformer(String configString) {
        this.lines = configString.split("\n");
        next();
    }

    /**
     * Inserts lines before the current key of the config.
     *
     * @param insertion the lines to insert.
     */
    public void insertLinesOnCurrentKey(String... insertion) {
        String indent = StringUtils.repeat(" ", this.lastIndent * 2);
        insertion = Arrays.stream(insertion).map(str -> indent + str).toArray(String[]::new);
        this.transformedLines.addAll(linesInserted + this.lastKeyIndex - 1, Arrays.asList(insertion));
        linesInserted += insertion.length;
    }

    public void insertComment(String... insertion) {
        for (String str : this.junk) {
            if (str.trim().startsWith("#"))
                return;
        }
        insertLinesOnCurrentKey(insertion);
    }

    @Override
    public boolean hasNext() {
        return nextKey != null;
    }

    @Override
    public String next() {
        String line = null;
        try {
            lastKeyIndex = index;

            junk.clear();
            List<String> temp = junk;
            junk = junk2;
            junk2 = temp;

            while (index < lines.length && !isKeyLine(line = lines[index++])) {
                junk2.add(line);
                this.transformedLines.add(line);
            }

            // Failed to find anything, return null
            if (!isKeyLine(line) && index == lines.length) {
                if (nextKey != null) {
                    String toReturn = nextKey;
                    nextKey = null;
                    lastIndent = indent;
                    return toReturn;
                }
                // Check if we should throw an exception.
                if (lastKeyIndex > 0)
                    throw new NoSuchElementException();
                return null;
            }

            // Update indent and key path
            int nextIndent = getIndent(line);
            keys = keys.subList(0, Math.min(nextIndent, keys.size()));
            String strippedLine = StringUtils.strip(line);
            if (strippedLine.charAt(0) == '\'')
                strippedLine = strippedLine.replace("'", "");
            keys.add(strippedLine.substring(0, strippedLine.indexOf(":")));

            // Build key, update next
            transformedLines.add(line);
            String previous = nextKey;
            String fullKey = String.join(".", keys);
            lastIndent = indent;
            indent = nextIndent;
            nextKey = fullKey;
            return previous;
        } catch (Exception exc) {
            KCommon.getInstance().getLogger().severe("Failed to read config! Current Line: " + line + " " + this);
            throw exc;
        }

    }

    /**
     * Gets the indentation of the given line.
     *
     * @param line the line.
     * @return the line indent.
     */
    private static int getIndent(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                indent = i / 2;
                break;
            }
        }
        return indent;
    }

    /**
     * Checks if the given line is a key in the config.
     *
     * @param line the line.
     * @return the key line.
     */
    private static boolean isKeyLine(String line) {
        if (StringUtils.isBlank(line))
            return false;

        String strippedLine = StringUtils.strip(line);
        return strippedLine.contains(":") && (Character.isLetterOrDigit(strippedLine.charAt(0)) || strippedLine.charAt(0) == '\'');
    }

    @NotNull
    @Override
    public Iterator<String> iterator() {
        return this; // :)
    }

    @Override
    public String toString() {
        return "ConfigTransformer{" +
                "lastKeyIndex=" + lastKeyIndex +
                ", linesInserted=" + linesInserted +
                ", junk=" + junk +
                ", junk2=" + junk2 +
                ", index=" + index +
                ", indent=" + indent +
                ", lastIndent=" + lastIndent +
                ", keys=" + keys +
                ", nextKey='" + nextKey + '\'' +
                '}';
    }
}
