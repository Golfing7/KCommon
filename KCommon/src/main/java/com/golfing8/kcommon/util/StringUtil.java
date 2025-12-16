package com.golfing8.kcommon.util;

import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Contains utility methods for Strings
 */
@UtilityClass
public class StringUtil {

    private static final DecimalFormat MONEY_FORMAT = new DecimalFormat("###,###,###,###,###.##");
    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("###,###,###,###,###");

    /**
     * Contains font information for the default MC font
     */
    @Getter
    public enum DefaultFontInfo {

        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PERENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('a', 4);

        private final char character;
        private final int length;

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        /**
         * Gets the length when this character is bolded
         *
         * @return the bolded length
         */
        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        /**
         * Gets the default font info for the given character
         *
         * @param c the character
         * @return the font info
         */
        public static DefaultFontInfo getDefaultFontInfo(char c) {
            for (DefaultFontInfo dFI : DefaultFontInfo.values()) {
                if (dFI.getCharacter() == c) return dFI;
            }
            return DefaultFontInfo.DEFAULT;
        }
    }

    private final static int CENTER_PX = 154;

    /**
     * Converts a camelCasedString to a yaml-cased-string.
     *
     * @param camelCase the camel case string.
     * @return the yaml cased string.
     */
    public static String camelToYaml(String camelCase) {
        StringBuilder builder = new StringBuilder();
        boolean lastLower = false;
        for (char c : camelCase.toCharArray()) {
            if (Character.isUpperCase(c) && lastLower) {
                lastLower = false;
                builder.append("-").append(Character.toLowerCase(c));
            } else {
                lastLower = Character.isLowerCase(c);
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    /**
     * Converts an ENUM_CASED_STRING to a yaml-cased-string
     *
     * @param enumCase the enum cased string.
     * @return the yaml cased string.
     */
    public static String enumToYaml(String enumCase) {
        return enumCase.toLowerCase().replace("_", "-").replace("$", ".");
    }

    /**
     * Strips the suffix from the given string.
     *
     * @param message  the message.
     * @param suffixes the suffixes.
     * @return the stripped suffix.
     */
    public static String stripSuffixes(String message, String... suffixes) {
        for (String suffix : suffixes) {
            if (message.endsWith(suffix)) {
                return message.substring(0, message.length() - suffix.length());
            }
        }
        return message;
    }

    // Taken from spigot

    /**
     * Sends a message that is centered in the player's chat
     *
     * @param player the player
     * @param message the message
     */
    public static void sendCenteredMessage(Player player, String message) {
        if (message == null || message.equals("")) {
            player.sendMessage("");
            return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        player.sendMessage(sb + message);
    }

    /**
     * Centers the given string for sending in a player's chat
     *
     * @param message the message
     * @return the centered message
     */
    public static String centerMessage(String message) {
        message = ChatColor.translateAlternateColorCodes('&', message);

        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;

        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
            } else if (previousCode) {
                previousCode = false;
                isBold = c == 'l';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        int compensated = 0;
        StringBuilder sb = new StringBuilder();
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += spaceLength;
        }
        return sb + message;
    }

    /**
     * Places commas into the given string, assuming it is an integer.
     *
     * @param string the string
     * @return the new string
     */
    public static String parseCommas(String string) {
        String[] split = string.split("\\.");

        string = split[0];

        String appendAtEnd = split.length > 1 ? "." + split[1] : "";

        String backwards = new StringBuilder(string).reverse().toString();

        StringBuilder toReturn = new StringBuilder();

        if (string.length() < 4) return string;

        for (int z = 0; z < string.length(); z++) {
            if (z % 3 == 0 && z != 0) {
                toReturn.append(",");
            }
            toReturn.append(backwards.charAt(z));
        }

        toReturn.reverse();

        toReturn.append(appendAtEnd);
        return toReturn.toString();
    }

    /**
     * Places commas into the given integer
     *
     * @param integer the integer
     * @return the formatted string
     */
    public static String parseCommas(int integer) {
        return COMMA_FORMAT.format(integer);
    }

    /**
     * Places commas into the given integer
     *
     * @param integer the integer
     * @return the formatted string
     */
    public static String parseCommas(long integer) {
        return COMMA_FORMAT.format(integer);
    }

    /**
     * Places commas into the given number, truncating any decimal portion
     *
     * @param d the number
     * @return the formatted string
     */
    public static String parseCommas(double d) {
        return COMMA_FORMAT.format(d);
    }

    /**
     * Formats the given number with the {@link #MONEY_FORMAT} decimal formatter
     *
     * @param d the number
     * @return the formatted number
     */
    public static String parseMoney(double d) {
        return MONEY_FORMAT.format(d);
    }

    /**
     * Formats the given duration in seconds into a string like so
     * {@code SSs MMm HHh DDd} or
     * {@code DDd HHh MMm SSs} if {@code dayFirst} is true
     *
     * @param duration the duration in seconds
     * @param dayFirst true for days being placed first in format
     * @return the time formatted string
     */
    public static String timeFormatted(int duration, boolean dayFirst) {
        if (duration <= 0)
            return "0s";

        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = ((duration / 60) / 60) % 24;
        int days = ((duration / 60) / 60) / 24;

        String secondAppend = seconds + "s ";

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend + secondAppend).trim() : (secondAppend + minuteAppend + hourAppend + dayAppend).trim();
    }

    /**
     * Formats the given duration in minutes into a string like so
     * {@code MMm HHh DDd} or
     * {@code DDd HHh MMm} if {@code dayFirst} is true
     *
     * @param duration the duration in minutes
     * @param dayFirst true for days being placed first in format
     * @return the time formatted string
     */
    public static String timeFormattedNoSeconds(int duration, boolean dayFirst) {
        if (duration <= 0)
            return "0m";

        int minutes = duration % 60;
        int hours = (duration / 60) % 24;
        int days = (duration / 60) / 24;

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend).trim() : (minuteAppend + hourAppend + dayAppend).trim();
    }

    /**
     * Formats the given duration in seconds into a string like so
     * {@code SSs MMm HHh DDd}
     * <p>
     * If {@code duration} is divisible by 60 (no seconds portion), the SSs part of the string format is left out
     * </p>
     *
     * @param duration the duration in seconds
     * @return the time formatted string
     */
    public static String timeFormattedOptionalSeconds(int duration) {
        if (duration <= 0)
            return "0s";

        int seconds = duration % 60;
        int minutes = (duration / 60) % 60;
        int hours = ((duration / 60) / 60) % 24;
        int days = ((duration / 60) / 60) / 24;

        String secondAppend = minutes == 0 && hours == 0 && days == 0 ? seconds + "s " : "";

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + "m " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + "h " : "";

        String dayAppend = days != 0 ? days + "d " : "";

        return (dayAppend + hourAppend + minuteAppend + secondAppend).trim();
    }

    /**
     * Formats the given duration in minutes into a string like so
     * {@code MM minutes HH hours DD days} or
     * {@code DD days HH hours MM minutes} if {@code dayFirst} is true
     *
     * @param duration the duration in minutes
     * @param dayFirst true for days being placed first in format
     * @return the time formatted string
     */
    public static String timeFormattedNoSecondsExtended(int duration, boolean dayFirst) {
        if (duration <= 0)
            return "0 minutes";

        int minutes = duration % 60;
        int hours = (duration / 60) % 24;
        int days = (duration / 60) / 24;

        String minuteAppend = minutes != 0 || hours != 0 || days != 0 ? minutes + " minutes " : "";

        String hourAppend = hours != 0 || days != 0 ? hours + " hours " : "";

        String dayAppend = days != 0 ? days + " days " : "";

        return dayFirst ? (dayAppend + hourAppend + minuteAppend).trim() : (minuteAppend + hourAppend + dayAppend).trim();
    }

    /**
     * Formats the given duration in minutes into a string like so
     * {@code MM:SS}
     *
     * @param duration the duration in minutes
     * @return the time formatted string
     */
    public static String timeFormattedPotion(int duration) {
        if (duration <= 0)
            return "00:00";

        int seconds = duration % 60;
        int minutes = duration / 60;

        String secondAppend = seconds + "";

        if (secondAppend.length() < 2) secondAppend = "0" + secondAppend;

        String minuteAppend = minutes != 0 ? minutes + ":" : "";

        if (minuteAppend.length() < 2) minuteAppend = "0" + minuteAppend;

        return (minuteAppend + secondAppend).trim();
    }

    /**
     * Formats the given string with word capitalization
     * <p>
     * i.e. {@code this phrASe} becomes {@code This Phrase}
     * </p>
     *
     * @param string the string
     * @return the new string
     */
    public static String capitalize(String string) {
        String[] strings = string.replace("_", " ").toLowerCase().split(" ");

        StringBuilder toReturn = new StringBuilder();

        for (String next : strings) {
            String nextFirst = next.substring(0, 1);
            String nextSecond = next.substring(1);

            toReturn.append(nextFirst.toUpperCase()).append(nextSecond.toLowerCase()).append(" ");
        }

        return toReturn.toString().trim();
    }

    /**
     * Formats a location to a nice string
     *
     * @param location     the location
     * @param includeWorld if the world should be included
     * @return the string
     */
    public static String formatLocation(Location location, boolean includeWorld) {
        if (includeWorld) {
            return String.format("X:%.1f, Y:%.1f, Z:%.1f %s", location.getX(), location.getY(), location.getZ(), location.getWorld().getName());
        } else {
            return String.format("X:%.1f, Y:%.1f, Z:%.1f", location.getX(), location.getY(), location.getZ());
        }
    }

    /**
     * Checks if the given char sequence should be considered empty
     *
     * @param sequence the char sequence
     * @return true if the sequence is null or empty
     */
    public static boolean isEmpty(CharSequence sequence) {
        return sequence == null || sequence.length() == 0;
    }

    /**
     * Checks if the given char sequence is not empty
     *
     * @param sequence the char sequence
     * @return true if the sequence has at least one character
     */
    public static boolean isNotEmpty(CharSequence sequence) {
        return !isEmpty(sequence);
    }

    private final static TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    /**
     * Converts the given integer to a roman numeral
     *
     * @param number the number
     * @return the roman numeral
     */
    public static String toRoman(int number) {
        if (number == 0)
            return "0";
        boolean negative = number < 0;
        if (negative)
            number = -number;

        int l = ROMAN_NUMERALS.floorKey(number);
        if (number == l) {
            return ROMAN_NUMERALS.get(number);
        }
        return (negative ? "-" : "") + ROMAN_NUMERALS.get(l) + toRoman(number - l);
    }

    /**
     * Computes the levenshtein distance of the two char sequences
     *
     * @param lhs the first string
     * @param rhs the second string
     * @return the levenshtein distance
     */
    public static int levenshteinDistance(CharSequence lhs, CharSequence rhs) {
        return levenshteinDistance(lhs, rhs, 1, 1, 1, 1);
    }

    /**
     * Computes the levenshtein distance of the two char sequences
     *
     * @param source the first string
     * @param target the second string
     * @param deleteCost the cost for deletion
     * @param insertCost the cost for insertion
     * @param replaceCost the cost for replacing
     * @param swapCost the cost for swapping
     * @return the levenshtein distance
     */
    public static int levenshteinDistance(CharSequence source, CharSequence target,
                                          int deleteCost, int insertCost,
                                          int replaceCost, int swapCost) {
        /*
         * Required to facilitate the premise to the algorithm that two swaps of the
         * same character are never required for optimality.
         */
        if (2 * swapCost < insertCost + deleteCost) {
            throw new IllegalArgumentException("Unsupported cost assignment");
        }
        if (source.length() == 0) {
            return target.length() * insertCost;
        }
        if (target.length() == 0) {
            return source.length() * deleteCost;
        }
        int[][] table = new int[source.length()][target.length()];
        Map<Character, Integer> sourceIndexByCharacter = new HashMap<>();
        if (source.charAt(0) != target.charAt(0)) {
            table[0][0] = Math.min(replaceCost, deleteCost + insertCost);
        }
        sourceIndexByCharacter.put(source.charAt(0), 0);
        for (int i = 1; i < source.length(); i++) {
            int deleteDistance = table[i - 1][0] + deleteCost;
            int insertDistance = (i + 1) * deleteCost + insertCost;
            int matchDistance = i * deleteCost + (source.charAt(i) == target.charAt(0) ? 0 : replaceCost);
            table[i][0] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }
        for (int j = 1; j < target.length(); j++) {
            int deleteDistance = (j + 1) * insertCost + deleteCost;
            int insertDistance = table[0][j - 1] + insertCost;
            int matchDistance = j * insertCost + (source.charAt(0) == target.charAt(j) ? 0 : replaceCost);
            table[0][j] = Math.min(Math.min(deleteDistance, insertDistance), matchDistance);
        }
        for (int i = 1; i < source.length(); i++) {
            int maxSourceLetterMatchIndex = source.charAt(i) == target.charAt(0) ? 0 : -1;
            for (int j = 1; j < target.length(); j++) {
                Integer candidateSwapIndex = sourceIndexByCharacter.get(target.charAt(j));
                int jSwap = maxSourceLetterMatchIndex;
                int deleteDistance = table[i - 1][j] + deleteCost;
                int insertDistance = table[i][j - 1] + insertCost;
                int matchDistance = table[i - 1][j - 1];
                if (source.charAt(i) != target.charAt(j)) {
                    matchDistance += replaceCost;
                } else {
                    maxSourceLetterMatchIndex = j;
                }
                int swapDistance;
                if (candidateSwapIndex != null && jSwap != -1) {
                    int iSwap = candidateSwapIndex;
                    int preSwapCost;
                    if (iSwap == 0 && jSwap == 0) {
                        preSwapCost = 0;
                    } else {
                        preSwapCost = table[Math.max(0, iSwap - 1)][Math.max(0, jSwap - 1)];
                    }
                    swapDistance = preSwapCost + (i - iSwap - 1) * deleteCost + (j - jSwap - 1) * insertCost + swapCost;
                } else {
                    swapDistance = Integer.MAX_VALUE;
                }
                table[i][j] = Math.min(Math.min(Math.min(deleteDistance, insertDistance), matchDistance), swapDistance);
            }
            sourceIndexByCharacter.put(source.charAt(i), i);
        }
        return table[source.length() - 1][target.length() - 1];
    }
}
