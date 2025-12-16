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
        String text = applyFormats(in);
        Matcher m = hex.matcher(text);
        while (m.find()) {
            String hexCode = m.group();
            text = text.replace(hexCode, toChatColor(hexCode));
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    private String fixFormat2(String input) {
        String text = input;
        Matcher m = fix2.matcher(text);
        while (m.find()) {
            String hexcode = m.group();
            String fixed = hexcode.substring(2, 8);
            text = text.replace(hexcode, "#" + fixed);
        }
        return text;
    }

    private String fixFormat3(String input) {
        String text = input;
        Matcher m = fix3.matcher(text);
        while (m.find()) {
            String hexcode = m.group();
            String fixed = new String(new char[]{
                    hexcode.charAt(3),
                    hexcode.charAt(5),
                    hexcode.charAt(7),
                    hexcode.charAt(9),
                    hexcode.charAt(11),
                    hexcode.charAt(13)
            });
            text = text.replace(hexcode, "#" + fixed);
        }
        return text;
    }
}
