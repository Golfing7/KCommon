package com.golfing8.kcommon.nms.unknown;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtils {

    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final Pattern AMPERSAND_RGB_3 = Pattern.compile("&#([\\da-fA-F]{3})");
    private static final Pattern AMPERSAND_RGB_6 = Pattern.compile("&#([\\da-fA-F]{6})");
    private static final Pattern AMPERSAND_RGB_SPIGOT = Pattern.compile("&x(&[\\da-fA-F]){6}");
    private static final Pattern SECTION_RGB_3 = Pattern.compile("\u00A7#([\\da-fA-F]{3})");
    private static final Pattern SECTION_RGB_6 = Pattern.compile("\u00A7#([\\da-fA-F]{6})");
    private static final Pattern SECTION_RGB_SPIGOT = Pattern.compile("\u00A7x(\u00A7[\\da-fA-F]){6}");
    private static final Map<String, String> legacyColorMap = new HashMap<>(){{
                put("0", "<black>");
                put("1", "<dark_blue>");
                put("2", "<dark_green>");
                put("3", "<dark_aqua>");
                put("4", "<dark_red>");
                put("5", "<dark_purple>");
                put("6", "<gold>");
                put("7", "<gray>");
                put("8", "<dark_gray>");
                put("9", "<blue>");
                put("a", "<green>");
                put("b", "<aqua>");
                put("c", "<red>");
                put("d", "<light_purple>");
                put("e", "<yellow>");
                put("f", "<white>");
                put("n", "<underlined>");
                put("m", "<strikethrough>");
                put("k", "<obfuscated>");
                put("o", "<italic>");
                put("l", "<bold>");
                put("r", "<reset>");
    }};

    /**
     * Converts a {@link String} into a {@link Component}
     *
     * @param message      the string to convert
     * @return the component
     */
    public static Component toComponent(String message) {
        return miniMessage.deserialize(processLine(message)).decoration(TextDecoration.ITALIC, false);
    }

    public static String processLine(String str) {
        str = replaceLegacyColors(str, '&');
        str = replaceLegacyColors(str, '\u00A7');
        str = StringEscapeUtils.unescapeJava(str);
        return str;
    }

    /**
     * Converts a {@link java.util.List<String>} into a {@link Component}
     *
     * @param lines        the list with strings to convert
     * @return the component
     */
    public static Component toComponent(List<String> lines) {
        Component mainComponent = Component.empty();
        Iterator<String> iterator = lines.iterator();

        while (iterator.hasNext()) {
            String line = iterator.next();
            mainComponent = mainComponent.append(toComponent(line));

            if (iterator.hasNext()) {
                mainComponent = mainComponent.append(Component.newline());
            }
        }

        return mainComponent;
    }

    /**
     * Replaces the certain things from the message (&0 or §c) depending on what
     * character you provide.
     *
     * @param message   the message you want to replace the colors from
     * @param character the character you want to replace (most likely only § and &)
     * @return the string with the replaced colors
     */
    private static String replaceColors(String message, char character) {
        for(Map.Entry<String, String> entry : legacyColorMap.entrySet()) {
            message = message.replaceAll(character + entry.getKey(), entry.getValue());
        }

        return message;
    }

    /**
     * Replaces all legacy hex / color codes with the ones we need to support in {@link MiniMessage}.
     *
     * @param message the message to replace the hex codes / colors in
     * @param colorChar the color character to use
     * @return the string with the replaced colors
     */
    private static String replaceLegacyColors(String message, char colorChar) {
        Pattern sixCharHex = colorChar == '&' ? AMPERSAND_RGB_6 : SECTION_RGB_6;
        Matcher matcher = sixCharHex.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = (new StringBuilder(14)).append("<reset>").append(colorChar).append("x");
            for (char character : matcher.group(1).toCharArray())
                replacement.append(colorChar).append(character);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        message = sb.toString();

        Pattern threeCharHex = colorChar == '&' ? AMPERSAND_RGB_3 : SECTION_RGB_3;
        matcher = threeCharHex.matcher(message);
        sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = (new StringBuilder(14)).append("<reset>").append(colorChar).append("x");
            for (char character : matcher.group(1).toCharArray())
                replacement.append(colorChar).append(character).append(colorChar).append(character);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        message = sb.toString();
        Pattern spigotHexPattern = colorChar == '&' ? AMPERSAND_RGB_SPIGOT : SECTION_RGB_SPIGOT;
        matcher = spigotHexPattern.matcher(message);
        sb = new StringBuilder();
        while (matcher.find()) {
            StringBuilder replacement = (new StringBuilder(9)).append("<reset>").append("<#");
            for (char character : matcher.group().toCharArray()) {
                if (character != colorChar && character != 'x') replacement.append(character);
            }
            replacement.append(">");
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        message = sb.toString();
        message = replaceColors(message, colorChar);

        return message;
    }

}
