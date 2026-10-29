package com.golfing8.kcommon.config.commented;

import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.util.StringUtilCore;
import it.unimi.dsi.fastutil.chars.CharSet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A utility class for recognizing config keys and allowing iteration over them.
 */
public class ConfigTransformer implements Iterator<String>, Iterable<String> {
    private static final CharSet INVALID_KEY_STARTS = CharSet.of(
            '#', '-', ':', '?', '|', '>', '&', '*', '!', '%', '[', ']', '{', '}', ',', '`'
    );
    /**
     * The index of the last key in the transformed lines list.
     */
    private int lastKeyIndex = -1;
    /**
     * The list of new lines to put into the config
     */
    @Getter
    private final List<String> transformedLines = new ArrayList<>();
    /**
     * The amount of lines that have been artificially inserted
     */
    private int linesInserted = 0;
    /**
     * Lines, up to the current key, that weren't keys.
     */
    @Getter
    private List<String> junk = new ArrayList<>(), junk2 = new ArrayList<>();
    /**
     * The index of the lines
     */
    private int index = 0;
    /**
     * The indent of the lines
     */
    private int indent = -1, lastIndent = -1;
    /**
     * A stack of key indentations
     */
    private Stack<LineInfo> keyIndentations = new Stack<>();
    /**
     * The lines of the config
     */
    private final String[] lines;
    /**
     * The list of keys making up the path of the current key
     */
    private List<String> keys = new ArrayList<>();
    /**
     * The key to return on the next {@link #next()} call.
     */
    private String nextKey;
    /**
     * The inline comment trailing the value of the current key, or null if it has none.
     */
    @Getter
    private String inlineComment;
    /**
     * The index of the last line holding the value of the current key.
     */
    private int valueEndIndex = -1;

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
        String indent = repeatSpaces(lastIndent);
        insertion = Arrays.stream(insertion).map(str -> indent + str).toArray(String[]::new);
        this.transformedLines.addAll(linesInserted + this.lastKeyIndex - 1, Arrays.asList(insertion));
        linesInserted += insertion.length;
    }

    /**
     * Inserts the comment at the current key
     *
     * @param insertion the insertion
     */
    public void insertComment(String... insertion) {
        for (String str : this.junk) {
            if (str.trim().startsWith("#"))
                return;
        }
        insertLinesOnCurrentKey(insertion);
    }

    /**
     * Appends the inline comment to the value of the current key, unless it already has one.
     *
     * @param comment the comment, starting with '#'
     */
    public void insertInlineComment(String comment) {
        if (inlineComment != null || valueEndIndex < 0)
            return;
        int lineIndex = linesInserted + valueEndIndex;
        transformedLines.set(lineIndex, transformedLines.get(lineIndex) + " " + comment);
        inlineComment = comment;
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

            LineInfo startInfo = this.keyIndentations.isEmpty() ? null : this.keyIndentations.peek();
            valueEndIndex = index - 1;
            inlineComment = startInfo == null ? null : startInfo.getInlineComment();
            char openQuote = startInfo == null ? 0 : startInfo.getOpenQuote();
            boolean foundKey = false;
            while (index < lines.length) {
                line = lines[index++];
                if (openQuote != 0) {
                    // Lines of a quoted value spanning multiple lines are neither keys nor junk.
                    int close = findClosingQuote(line, 0, openQuote);
                    if (close >= 0) {
                        openQuote = 0;
                        inlineComment = getComment(line, close + 1);
                    }
                    valueEndIndex = index - 1;
                } else if (isKeyLine(line)) {
                    foundKey = true;
                    break;
                } else if (startInfo == null || !startInfo.isBlockScalarStart() || getIndent(line) <= startInfo.getIndent()) {
                    // Scalar block content is not considered junk.
                    junk2.add(line);
                    // Plain values may continue on more indented lines.
                    if (startInfo != null && startInfo.isPlainValue() && getIndent(line) > startInfo.getIndent()
                            && !isBlank(line) && !line.trim().startsWith("#")) {
                        inlineComment = getComment(line, 0);
                        valueEndIndex = index - 1;
                    }
                }
                this.transformedLines.add(line);
            }

            // Failed to find anything, return null
            if (!foundKey) {
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
            LineInfo lineInfo = getLineInfo(line);
            // Clear out old indentations
            while (!this.keyIndentations.isEmpty() && this.keyIndentations.peek().getIndent() >= lineInfo.getIndent()) {
                this.keyIndentations.pop();
            }
            // Add our new indentations
            this.keyIndentations.push(lineInfo);

            int keyDepth = this.keyIndentations.size() - 1;
            keys = new ArrayList<>(keys.subList(0, Math.min(keyDepth, keys.size())));
            keys.add(lineInfo.getKeyLabel());

            // Build key, update next
            transformedLines.add(line);
            String previous = nextKey;
            String fullKey = String.join(".", keys);
            lastIndent = indent;
            indent = lineInfo.getIndent();
            nextKey = fullKey;
            return previous;
        } catch (NoSuchElementException exc) {
            throw exc;
        } catch (Exception exc) {
            if (Platform.isSet()) {
                Platform.get().getLogger().severe("Failed to read config! Current Line: " + line + " " + this);
            }
            throw exc;
        }

    }

    private static int getIndent(String line) {
        int indent = 0;
        for (int i = 0; i < line.length(); i++) {
            if (!Character.isWhitespace(line.charAt(i))) {
                indent = i;
                break;
            }
        }
        return indent;
    }

    /**
     * Gets the key from the line, given that it is a key line.
     *
     * @param line the full line
     * @return the parsed key, removing any quotes.
     */
    private static LineInfo getLineInfo(String line) {
        String stripLine = line.trim();
        int colon = findKeyColon(stripLine);
        // Unquote the key, replacing only the escapes of its quote style.
        String keyLabel;
        if (stripLine.charAt(0) == '\'') {
            keyLabel = stripLine.substring(1, stripLine.lastIndexOf('\'', colon)).replace("''", "'");
        } else if (stripLine.charAt(0) == '"') {
            // drops the backslash of every escape, so `\n` becomes `n`. Fine for `\"` and `\\` in keys.
            keyLabel = stripLine.substring(1, stripLine.lastIndexOf('"', colon)).replaceAll("\\\\(.)", "$1");
        } else {
            keyLabel = stripLine.substring(0, colon);
        }

        String valuePart = stripLine.substring(colon + 1).trim();
        char valueStart = valuePart.isEmpty() ? 0 : valuePart.charAt(0);
        boolean quoted = valueStart == '\'' || valueStart == '"';
        boolean blockScalarStart = valueStart == '|' || valueStart == '>';
        char openQuote = 0;
        String inlineComment;
        if (quoted) {
            int close = findClosingQuote(valuePart, 1, valueStart);
            openQuote = close < 0 ? valueStart : 0;
            inlineComment = close < 0 ? null : getComment(valuePart, close + 1);
        } else {
            inlineComment = getComment(valuePart, 0);
        }
        // A plain value continues on the following lines until a comment ends it.
        boolean plainValue = valueStart != 0 && !quoted && !blockScalarStart && inlineComment == null;
        return new LineInfo(blockScalarStart, keyLabel, getIndent(line), openQuote, plainValue, inlineComment);
    }

    /**
     * Finds the ':' that ends the key of a stripped line.
     *
     * @param stripLine the stripped line
     * @return the index of the ':', or -1 if the line has no key
     */
    private static int findKeyColon(String stripLine) {
        char first = stripLine.charAt(0);
        if (first == '\'' || first == '"') {
            int close = findClosingQuote(stripLine, 1, first);
            if (close < 0 || StringUtilCore.indexOfByRegex("^\\s*:(\\s|$)", stripLine, close + 1) < 0)
                return -1;
            return stripLine.indexOf(':', close + 1);
        }
        // Without quotes, the first `:` indicates the end of the key, unless a comment starts before it.
        int colon = StringUtilCore.indexOfByRegex(":(\\s|$)", stripLine);
        int comment = findComment(stripLine, 0);
        return comment >= 0 && comment < colon ? -1 : colon;
    }

    /**
     * Finds the quote closing a quoted scalar.
     *
     * @param line  the line
     * @param from  the index to search from, after the opening quote
     * @param quote the quote character
     * @return the index of the closing quote, or -1 if it isn't on this line
     */
    private static int findClosingQuote(String line, int from, char quote) {
        for (int i = from; i < line.length(); i++) {
            char c = line.charAt(i);
            if (quote == '"' && c == '\\') {
                i++;
            } else if (c == quote) {
                // `''` is an escaped quote in single quoted scalars.
                if (quote == '\'' && i + 1 < line.length() && line.charAt(i + 1) == '\'') {
                    i++;
                } else {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Finds an inline comment, a '#' at the start of the line or after whitespace.
     *
     * @param line the line, which must not be inside a quoted scalar from {@code from} onward
     * @param from the index to search from
     * @return the index of the '#', or -1 if there is no comment
     */
    private static int findComment(String line, int from) {
        for (int i = from; i < line.length(); i++) {
            if (line.charAt(i) == '#' && (i == 0 || Character.isWhitespace(line.charAt(i - 1))))
                return i;
        }
        return -1;
    }

    /**
     * Gets the inline comment of the line.
     *
     * @param line the line
     * @param from the index to search from
     * @return the comment, or null if there is none
     */
    private static String getComment(String line, int from) {
        int start = findComment(line, from);
        return start < 0 ? null : line.substring(start).replaceAll("\\s+$", "");
    }

    /**
     * Checks if the given line is a key in the config.
     *
     * @param line the line.
     * @return the key line.
     */
    @Contract("null -> false")
    private boolean isKeyLine(String line) {
        if (isBlank(line))
            return false;

        boolean scalarBlock = !keyIndentations.isEmpty() && keyIndentations.peek().isBlockScalarStart();
        if (scalarBlock) {
            int indent = getIndent(line);
            // if the indent is greater than the scalar block start, then it hasn't ended yet and this isn't a key line.
            if (indent > keyIndentations.peek().getIndent()) {
                return false;
            }
        }

        String strippedLine = line.trim();
        return !INVALID_KEY_STARTS.contains(strippedLine.charAt(0)) && findKeyColon(strippedLine) >= 0;
    }

    @Data
    @AllArgsConstructor
    private static final class LineInfo {
        private boolean blockScalarStart;
        private String keyLabel;
        private int indent;
        /**
         * The quote of a quoted value left open at the end of the line, or 0.
         */
        private char openQuote;
        /**
         * If the value is a plain scalar that may continue on the following lines.
         */
        private boolean plainValue;
        private String inlineComment;
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

    private static boolean isBlank(String line) {
        return line == null || line.trim().isEmpty();
    }

    private static String repeatSpaces(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }
        return builder.toString();
    }
}
