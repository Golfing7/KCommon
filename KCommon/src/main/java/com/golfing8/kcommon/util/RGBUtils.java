package com.golfing8.kcommon.util;

import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains useful methods for working with RGB chat colors and text
 */
public class RGBUtils {
    public static final RGBUtils INSTANCE = new RGBUtils();

    private final Pattern hex = Pattern.compile("&#[0-9a-fA-F]{6}");
    private final Pattern fix2 = Pattern.compile("\\{&#[0-9a-fA-F]{6}\\}");
    private final Pattern fix3 = Pattern.compile("&x[&0-9a-fA-F]{12}");

    private String toChatColor(String hexCode) {
        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        char[] var3 = hexCode.substring(2).toCharArray();

        for (char c : var3) {
            magic.append(ChatColor.COLOR_CHAR).append(c);
        }
        return magic.toString();
    }

    private String applyFormats(String textInput) {
        String text = textInput;
        text = fixFormat2(text);
        text = fixFormat3(text);
        return text;
    }

    /**
     * Formats the hex RGB colors in the given text
     *
     * @param in the text
     * @return the formatted text
     */
    public String hexColor(String in) {
        return ChatColor.translateAlternateColorCodes('&', convertHexCodes(in));
    }

    /**
     * Same as {@link #hexColor(String)}, but skips the trailing legacy '&amp;' color code translation
     * pass. Intended for callers (such as {@code MS#applyTransformers}) that have already translated
     * legacy '&amp;' codes to '§' upstream, where re-running the translation here would just be a
     * full-string no-op scan.
     *
     * @param in the text, with legacy '&amp;' codes already translated
     * @return the formatted text
     */
    public String hexColorLegacyAlreadyTranslated(String in) {
        return convertHexCodes(in);
    }

    private String convertHexCodes(String textInput) {
        String text = applyFormats(textInput);
        Matcher m = hex.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(toChatColor(m.group())));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String fixFormat2(String input) {
        Matcher m = fix2.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String fixed = m.group().substring(3, 9);
            m.appendReplacement(sb, Matcher.quoteReplacement("&#" + fixed));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private String fixFormat3(String input) {
        Matcher m = fix3.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String hexcode = m.group();
            String fixed = new String(new char[] {
                    hexcode.charAt(3),
                    hexcode.charAt(5),
                    hexcode.charAt(7),
                    hexcode.charAt(9),
                    hexcode.charAt(11),
                    hexcode.charAt(13)
            });
            m.appendReplacement(sb, Matcher.quoteReplacement("&#" + fixed));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
