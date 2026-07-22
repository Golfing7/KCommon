package com.golfing8.kcommon.util.string;

import com.golfing8.kcommon.struct.helper.function.Numbers;
import com.golfing8.kcommon.util.MathExpressions;
import com.golfing8.kcommon.util.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A class full of macros that can apply to a string.
 * <p>
 * All macros should take the form of $symbol(optionalarg1)(optionalarg2)(...){content}
 * and should operate on the content in the braces.
 * </p>
 */
public class StringMacros {
    public static final StringMacros DEFAULT;

    static {
        DEFAULT = new StringMacros();
        DEFAULT.registerMacro("lc", (args, string) -> {
            return string.toLowerCase();
        });
        DEFAULT.registerMacro("uc", (args, string) -> {
            return string.toUpperCase();
        });
        DEFAULT.registerMacro("cap", (args, string) -> {
            return StringUtil.capitalize(string);
        });
        DEFAULT.registerMacro("commas", (args, string) -> {
            return StringUtil.parseCommas(string);
        });
        DEFAULT.registerMacro("stripcommas", (args, string) -> {
            return string.replace(",", "");
        });
        DEFAULT.registerMacro("rs", (args, string) -> {
            if (string.isEmpty())
                return "";

            int numbers = 8;
            if (!args.isEmpty()) {
                numbers = Numbers.parseInteger(args.get(0)).orElse(numbers);
            }
            char[] characterPool = string.toCharArray();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < numbers; i++) {
                builder.append(characterPool[ThreadLocalRandom.current().nextInt(characterPool.length)]);
            }
            return builder.toString();
        });
        DEFAULT.registerMacro("sc", (args, string) -> {
            return ChatColor.stripColor(string);
        });
        DEFAULT.registerMacro("roman", (args, string) -> {
            return StringUtil.toRoman(Numbers.parseInteger(string).orElse(0));
        });
        DEFAULT.registerMacro("replace", (args, string) -> {
            if (args.size() < 2)
                return string;

            String pattern = args.get(0);
            String replacement = args.get(1);
            return string.replace(pattern, replacement);
        });
        DEFAULT.registerMacro("repeat", (args, string) -> {
            int count = 2;
            if (!args.isEmpty()) {
                count = Numbers.parseInteger(args.get(0)).orElse(count);
            }
            return StringUtils.repeat(string, count);
        });
        DEFAULT.registerMacro("eval", (args, string) -> {
            return StringUtil.formatDecimal(MathExpressions.evaluate(string));
        });
        DEFAULT.registerMacro("reverse", (args, string) -> {
            return StringUtils.reverse(string);
        });
        DEFAULT.registerMacro("substring", (args, string) -> {
            if (args.size() < 2)
                return string;

            int begin = Numbers.parseInteger(args.get(0)).orElse(0);
            int end = Numbers.parseInteger(args.get(1)).orElse(string.length());
            return string.substring(begin, end);
        });
        DEFAULT.registerMacro("int", (args, string) -> {
            return Numbers.parseDoubleOpt(string).map(StringUtil::formatInteger).orElse(string);
        });
    }

    /**
     * A functional interface for macros to use
     */
    @FunctionalInterface
    public interface MacroFunction {
        /**
         * @param args    List of arguments provided inside parenthesis: (arg1)(arg2)
         * @param content Evaluated content inside the curly braces: {content}
         * @return The transformed string result
         */
        String apply(List<String> args, String content);
    }

    private final Map<String, MacroFunction> macroRegistry = new HashMap<>();

    /**
     * Registers a new macro with the given symbol name.
     *
     * @param symbol The trigger word after $ (e.g., "upper" for $upper{...})
     * @param macro  The macro logic implementation
     */
    public void registerMacro(String symbol, MacroFunction macro) {
        macroRegistry.put(symbol, macro);
    }

    /**
     * Parses and evaluates all macros found in the input string.
     * Supports nested macros.
     */
    public String parse(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        char[] charArray = input.toCharArray();
        return parse(charArray, 0, charArray.length);
    }

    /**
     * Parses the character array with the beginning and end quickly
     *
     * @param inputCh the character array
     * @param begin the beginning index
     * @param end the ending index
     * @return the resulting string
     */
    private String parse(char[] inputCh, int begin, int end) {
        if (begin >= end)
            return "";

        StringBuilder result = new StringBuilder();
        int i = begin;
        int copyStart = begin;

        while (i < end) {
            if (inputCh[i] == '$') {
                int symbolStart = i + 1;
                int symbolEnd = symbolStart;

                // 1. Read macro symbol name (alphanumeric / underscores)
                while (symbolEnd < end && isFastMacroSymbolPart(inputCh[symbolEnd])) {
                    symbolEnd++;
                }

                String symbol = new String(inputCh, symbolStart, symbolEnd - symbolStart);

                MacroFunction macroFunction;
                if (!symbol.isEmpty() && (macroFunction = macroRegistry.get(symbol)) != null) {
                    int curr = symbolEnd;
                    List<String> args = Collections.emptyList();

                    // 2. Parse zero or more optional arguments in parentheses: (arg1)(arg2)
                    if (curr < end && inputCh[curr] == '(') {
                        args = new ArrayList<>(2);
                        while (curr < end && inputCh[curr] == '(') {
                            int argEnd = findMatchingCloser(inputCh, curr, '(', ')');
                            if (argEnd == -1) break;

                            args.add(parse(inputCh, curr + 1, argEnd));
                            curr = argEnd + 1;
                        }
                    }

                    // 3. Parse required content body in curly braces: {content}
                    if (curr < end && inputCh[curr] == '{') {
                        int contentEnd = findMatchingCloser(inputCh, curr, '{', '}');
                        if (contentEnd != -1) {
                            // Bulk-append buffered plain text before macro output
                            if (i > copyStart) {
                                result.append(inputCh, copyStart, i - copyStart);
                            }

                            // Process nested macros inside the body content first
                            String evaluatedContent = parse(inputCh, curr + 1, contentEnd);

                            // Apply registered macro logic
                            result.append(macroFunction.apply(args, evaluatedContent));

                            i = contentEnd + 1;
                            copyStart = i; // Move copy index past the macro
                            continue;
                        }
                    }
                }
            }
            i++;
        }

        // Bulk-append remaining plain text
        if (i > copyStart) {
            result.append(inputCh, copyStart, i - copyStart);
        }

        return result.toString();
    }

    /**
     * Helper to find matching closing bracket while correctly tracking nested pairs.
     */
    private int findMatchingCloser(char[] input, int startIdx, char openChar, char closeChar) {
        int depth = 0;
        for (int i = startIdx; i < input.length; i++) {
            char c = input[i];
            if (c == openChar) {
                depth++;
            } else if (c == closeChar) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1; // Unbalanced brackets
    }

    /**
     * Checks if the character is part of a macro
     *
     * @param c the character
     * @return true if part of a macro
     */
    private static boolean isFastMacroSymbolPart(char c) {
        return c >= 'a' && c <= 'z' ||
                c >= 'A' && c <= 'Z' ||
                c >= '0' && c <= '9' ||
                c == '_' || c == '$';
    }
}
