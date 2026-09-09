package com.golfing8.kcommon;

import lombok.experimental.UtilityClass;
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

/**
 * Contains utilities for interfacing with components
 */
@UtilityClass
public class ComponentUtils {

    private static final MiniMessage miniMessage;
    // Backreferenced so a single pass matches either the '&' or '§' variant while still requiring
    // a consistent color character across the whole match (e.g. &x&F&F... or §x§F§F..., never mixed).
    private static final Pattern RGB_3 = Pattern.compile("([&§])#([\\da-fA-F]{3})");
    private static final Pattern RGB_6 = Pattern.compile("([&§])#([\\da-fA-F]{6})");
    private static final Pattern RGB_SPIGOT = Pattern.compile("([&§])x(?:\\1[\\da-fA-F]){6}");
    private static final Map<Character, String> legacyColorMap = new HashMap<>();
    private static final int CENTER_PX = 154;
    /**
     * Bounds memory usage of {@link #COMPONENT_CACHE}. Components are immutable, so caching them by
     * their fully-transformed source string is always safe - this just bounds how many distinct
     * strings we remember at once (oldest/least-recently-used evicted first).
     */
    private static final int COMPONENT_CACHE_MAX_SIZE = 2048;
    /**
     * Caches the result of {@link #toComponent(String)} keyed by its fully-transformed input string.
     * Parsing the same message repeatedly (e.g. a static broadcast, or a repeating scoreboard/action
     * bar line whose placeholder values haven't changed) is otherwise pure repeated work: the
     * resulting {@link Component} is immutable, so sharing one cached instance across callers is safe.
     */
    private static final Map<String, Component> COMPONENT_CACHE = new LinkedHashMap<String, Component>(16, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
            return size() > COMPONENT_CACHE_MAX_SIZE;
        }
    };
    public static final BukkitAudiences bukkitAudiences = BukkitAudiences.create(Bukkit.getPluginManager().getPlugin("KCommon"));

    static {
        String resetFormat = "<!bold><!italic><!underlined><!strikethrough><!obfuscated>";
        legacyColorMap.put('0', resetFormat + "<black>");
        legacyColorMap.put('1', resetFormat + "<dark_blue>");
        legacyColorMap.put('2', resetFormat + "<dark_green>");
        legacyColorMap.put('3', resetFormat + "<dark_aqua>");
        legacyColorMap.put('4', resetFormat + "<dark_red>");
        legacyColorMap.put('5', resetFormat + "<dark_purple>");
        legacyColorMap.put('6', resetFormat + "<gold>");
        legacyColorMap.put('7', resetFormat + "<gray>");
        legacyColorMap.put('8', resetFormat + "<dark_gray>");
        legacyColorMap.put('9', resetFormat + "<blue>");
        legacyColorMap.put('a', resetFormat + "<green>");
        legacyColorMap.put('b', resetFormat + "<aqua>");
        legacyColorMap.put('c', resetFormat + "<red>");
        legacyColorMap.put('d', resetFormat + "<light_purple>");
        legacyColorMap.put('e', resetFormat + "<yellow>");
        legacyColorMap.put('f', resetFormat + "<white>");
        legacyColorMap.put('n', "<underlined>");
        legacyColorMap.put('m', "<strikethrough>");
        legacyColorMap.put('k', "<obfuscated>");
        legacyColorMap.put('o', "<italic>");
        legacyColorMap.put('l', "<bold>");
        legacyColorMap.put('r', "<reset>");

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

        synchronized (COMPONENT_CACHE) {
            Component cached = COMPONENT_CACHE.get(message);
            if (cached != null)
                return cached;
        }

        Component deserialize = miniMessage.deserialize(processLine(message));
        if (!deserialize.hasDecoration(TextDecoration.ITALIC)) {
            deserialize = deserialize.decoration(TextDecoration.ITALIC, false);
        }

        synchronized (COMPONENT_CACHE) {
            COMPONENT_CACHE.put(message, deserialize);
        }
        return deserialize;
    }

    /**
     * Processes the given line
     *
     * @param str the string
     * @return the processed string
     */
    public static String processLine(String str) {
        str = replaceLegacyColors(str);
        str = replaceColors(str);
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
     * Replaces legacy formatting codes (e.g. &amp;a or §a) with their {@link MiniMessage} tag
     * equivalents, in a single left-to-right pass recognizing both '&amp;' and '§' as the color
     * character.
     *
     * @param message the message you want to replace the colors from
     * @return the string with the replaced colors
     */
    private static String replaceColors(String message) {
        int length = message.length();
        StringBuilder result = new StringBuilder(length);
        int i = 0;
        while (i < length) {
            char c = message.charAt(i);
            if ((c == '&' || c == '§') && i + 1 < length) {
                String tag = legacyColorMap.get(message.charAt(i + 1));
                if (tag != null) {
                    result.append(tag);
                    i += 2;
                    continue;
                }
            }
            result.append(c);
            i++;
        }
        return result.toString();
    }

    /**
     * Replaces all legacy hex / color codes with the ones we need to support in {@link MiniMessage}.
     * Recognizes both '&amp;' and '§' as the color character in a single pass per hex format
     * (rather than running the whole pipeline once per character).
     *
     * @param message the message to replace the hex codes / colors in
     * @return the string with the replaced colors
     */
    private static String replaceLegacyColors(String message) {
        Matcher matcher = RGB_6.matcher(message);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            char colorChar = matcher.group(1).charAt(0);
            StringBuilder replacement = (new StringBuilder(14)).append("<reset>").append(colorChar).append("x");
            for (char character : matcher.group(2).toCharArray())
                replacement.append(colorChar).append(character);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);
        message = sb.toString();

        matcher = RGB_3.matcher(message);
        sb = new StringBuffer();
        while (matcher.find()) {
            char colorChar = matcher.group(1).charAt(0);
            StringBuilder replacement = (new StringBuilder(14)).append("<reset>").append(colorChar).append("x");
            for (char character : matcher.group(2).toCharArray())
                replacement.append(colorChar).append(character).append(colorChar).append(character);
            matcher.appendReplacement(sb, replacement.toString());
        }
        matcher.appendTail(sb);

        message = sb.toString();
        matcher = RGB_SPIGOT.matcher(message);
        sb = new StringBuffer();
        while (matcher.find()) {
            char colorChar = matcher.group(1).charAt(0);
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
