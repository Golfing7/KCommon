package com.golfing8.kcommon;

import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.map.MinecraftFont;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComponentUtils {

    private static final MiniMessage miniMessage;
    private static final Pattern AMPERSAND_RGB_3 = Pattern.compile("&#([\\da-fA-F]{3})");
    private static final Pattern AMPERSAND_RGB_6 = Pattern.compile("&#([\\da-fA-F]{6})");
    private static final Pattern AMPERSAND_RGB_SPIGOT = Pattern.compile("&x(&[\\da-fA-F]){6}");
    private static final Pattern SECTION_RGB_3 = Pattern.compile("\u00A7#([\\da-fA-F]{3})");
    private static final Pattern SECTION_RGB_6 = Pattern.compile("\u00A7#([\\da-fA-F]{6})");
    private static final Pattern SECTION_RGB_SPIGOT = Pattern.compile("\u00A7x(\u00A7[\\da-fA-F]){6}");
    private static final Map<String, String> legacyColorMap = new HashMap<>();
    private static final int CENTER_PX = 154;
    public static final BukkitAudiences bukkitAudiences = BukkitAudiences.create(Bukkit.getPluginManager().getPlugin("KCommon"));

    static {
        legacyColorMap.put("0", "<reset><black>");
        legacyColorMap.put("1", "<reset><dark_blue>");
        legacyColorMap.put("2", "<reset><dark_green>");
        legacyColorMap.put("3", "<reset><dark_aqua>");
        legacyColorMap.put("4", "<reset><dark_red>");
        legacyColorMap.put("5", "<reset><dark_purple>");
        legacyColorMap.put("6", "<reset><gold>");
        legacyColorMap.put("7", "<reset><gray>");
        legacyColorMap.put("8", "<reset><dark_gray>");
        legacyColorMap.put("9", "<reset><blue>");
        legacyColorMap.put("a", "<reset><green>");
        legacyColorMap.put("b", "<reset><aqua>");
        legacyColorMap.put("c", "<reset><red>");
        legacyColorMap.put("d", "<reset><light_purple>");
        legacyColorMap.put("e", "<reset><yellow>");
        legacyColorMap.put("f", "<reset><white>");
        legacyColorMap.put("n", "<underlined>");
        legacyColorMap.put("m", "<strikethrough>");
        legacyColorMap.put("k", "<obfuscated>");
        legacyColorMap.put("o", "<italic>");
        legacyColorMap.put("l", "<bold>");
        legacyColorMap.put("r", "<reset>");

        miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Converts a {@link String} into a {@link Component}
     *
     * @param message the string to convert
     * @return the component
     */
    public static Component toComponent(String message) {
        if (message == null)
            return null;

        return miniMessage.deserialize(processLine(message)).decoration(TextDecoration.ITALIC, false);
    }

    public static String processLine(String str) {
        str = replaceLegacyColors(str, '&');
        str = replaceLegacyColors(str, '\u00A7');
        str = replaceColors(str, '&');
        str = replaceColors(str, '\u00A7');
        str = StringEscapeUtils.unescapeJava(str);
        str = maybeCenter(str);
        return str;
    }

    /**
     * Converts a {@link List} into a single {@link Component}
     *
     * @param lines the list with strings to convert
     * @return the component
     */
    public static Component toFlatComponent(List<@NotNull String> lines) {
        if (lines.isEmpty())
            return Component.empty();

        Component parentComponent = toComponent(lines.get(0));
        for (String line : lines.subList(1, lines.size())) {
            parentComponent = parentComponent.appendNewline().append(toComponent(line));
        }
        return parentComponent;
    }

    /**
     * Converts a {@link List} into a {@link Component}
     *
     * @param lines the list with strings to convert
     * @return the component
     */
    public static List<Component> toComponent(List<@NotNull String> lines) {
        if (lines.isEmpty())
            return Collections.emptyList();

        List<Component> components = new ArrayList<>();
        for (String line : lines) {
            components.add(toComponent(line));
        }

        return components;
    }

    /**
     * Centers the string if it needs to be.
     *
     * @param str the string
     * @return the new string
     */
    public static String maybeCenter(String str) {
        if (!str.startsWith("center::"))
            return str;

        str = str.substring(8);
        String asLegacy = LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(str));

        int messagePxSize = 0;
        boolean isBold = false;
        boolean previousColorChar = false;

        for (char c : asLegacy.toCharArray()) {
            if (c == ChatColor.COLOR_CHAR) {
                isBold = false;
                previousColorChar = true;
            } else if (previousColorChar) {
                if (c == 'l' || c == 'L') isBold = true;
                previousColorChar = false;
            } else {
                messagePxSize += MinecraftFont.Font.getChar(c).getWidth();

                if (isBold) messagePxSize += 1;
                messagePxSize++;
            }
        }
        int paddingSpaces = (CENTER_PX - (messagePxSize / 2)) / 4;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paddingSpaces; i++) {
            builder.append(" ");
        }
        return builder + str;
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
        for (Map.Entry<String, String> entry : legacyColorMap.entrySet()) {
            message = message.replace(character + entry.getKey(), entry.getValue());
        }

        return message;
    }

    /**
     * Replaces all legacy hex / color codes with the ones we need to support in {@link MiniMessage}.
     *
     * @param message   the message to replace the hex codes / colors in
     * @param colorChar the color character to use
     * @return the string with the replaced colors
     */
    private static String replaceLegacyColors(String message, char colorChar) {
        Pattern sixCharHex = colorChar == '&' ? AMPERSAND_RGB_6 : SECTION_RGB_6;
        Matcher matcher = sixCharHex.matcher(message);
        StringBuffer sb = new StringBuffer();
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
        sb = new StringBuffer();
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
        sb = new StringBuffer();
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

        return message;
    }

}
