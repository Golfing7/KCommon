package com.golfing8.kcommon.util;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.struct.title.Title;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Contains message utilities used for properly parsing things
 */
@UtilityClass
public final class MS {
    private static final List<Function<String, String>> TRANSFORMERS = Lists.newArrayList();

    static {
        //Color first, always
        TRANSFORMERS.add((str) -> ChatColor.translateAlternateColorCodes('&', str));

        //Try hex coloring, if we're on a version which supports it.
        TRANSFORMERS.add(string -> {
            if(KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_16))
                return RGBUtils.INSTANCE.hexColor(string);
            return string;
        });

        //Lower case something
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$lc\\{[^}]+}");
            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    s = s.replace(grouped, grouped.replace("$lc{", "").replace("}", "").toLowerCase(Locale.ROOT));
                }
                return s;
            }
        });

        //Upper case something
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$uc\\{[^}]+}");
            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    s = s.replace(grouped, grouped.replace("$uc{", "").replace("}", "").toUpperCase(Locale.ROOT));
                }
                return s;
            }
        });

        //Properly capitalize something
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$cap\\{[^}]+}");
            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    s = s.replace(grouped, StringUtil.capitalize(grouped.replace("$cap{", "").replace("}", "")));
                }
                return s;
            }
        });

        // Parses commas into a number.
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$commas\\{[^}]+}");

            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    s = s.replace(grouped, StringUtil.parseCommas(grouped.replace("$commas{", "").replace("}", "")));
                }
                return s;
            }
        });

        //Generate a random string made of all the characters provided in the {}.
        TRANSFORMERS.add(new Function<String, String>() {
            private static final int DEFAULT_LENGTH = 8;
            final Pattern pattern = Pattern.compile("\\$rs\\([0-9]+\\)\\{[^}]+}");
            final Pattern numberMatcher = Pattern.compile("\\([0-9]+\\)");
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    Matcher numbers = numberMatcher.matcher(grouped);

                    int characters = DEFAULT_LENGTH;

                    String numGroup = "";

                    if(numbers.find()){
                        numGroup = numbers.group();

                        characters = Integer.parseInt(numGroup.replace("(", "").replace(")", ""));
                    }

                    char[] characterPool = s.replace(grouped, grouped.replace("$rs" + numGroup + "{", "")
                            .replace("}", "")).toCharArray();

                    StringBuilder builder = new StringBuilder();

                    for (int i = 0; i < characters; i++) {
                        builder.append(characterPool[ThreadLocalRandom.current().nextInt(characterPool.length)]);
                    }

                    s = s.replace(grouped, builder.toString());
                }
                return s;
            }
        });

        //Strip color
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$sc\\{[^}]+}");
            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);

                    s = s.replace(grouped, ChatColor.stripColor(grouped.replace("$sc{", "").replace("}", "")));
                }
                return s;
            }
        });

        //To roman numeral
        TRANSFORMERS.add(new Function<String, String>() {
            final Pattern pattern = Pattern.compile("\\$roman\\{(\\d+)}");
            @Override
            public String apply(String s) {
                Matcher matcher = pattern.matcher(s);

                while(matcher.find()){
                    String grouped = matcher.group(0);
                    String number = matcher.group(1);

                    try {
                        int numeral = Integer.parseInt(number);
                        s = s.replace(grouped, StringUtil.toRoman(numeral));
                    } catch (NumberFormatException ignored) {}
                }
                return s;
            }
        });
    }

    @Contract(pure = true)
    public static @NotNull String applyTransformers(@NotNull String str) {
        for(Function<String, String> func : TRANSFORMERS){
            str = func.apply(str);
        }
        return str;
    }

    /**
     * Parses all the messages by calling parseSingle for every message.
     *
     * @param messages the messages to parse
     * @param placeholders the placeholders to use
     * @return the built list
     */
    public static List<String> parseAll(List<String> messages, Object... placeholders) {
        return PlaceholderContainer.compileTrusted(placeholders).applyTrusted(messages).stream().map(MS::applyTransformers).collect(Collectors.toList());
    }

    /**
     * Parses a single string.
     *
     * @param message the message
     * @param placeholders the placeholders
     * @return the parsed string.
     */
    public static String parseSingle(String message, Object... placeholders){
        if(message == null)
            return null;

        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        message = container.applyTrusted(Collections.singletonList(message)).get(0);
        message = applyTransformers(message);
        return message;
    }

    /**
     * Converts the message to a component.
     *
     * @param message the message
     * @param placeholders the placeholders
     * @return the component
     */
    public static Component toComponent(String message, Object... placeholders) {
        if (StringUtil.isEmpty(message))
            return Component.empty();

        return toComponent(Collections.singletonList(message), placeholders);
    }

    /**
     * Converts the message to a component.
     *
     * @param message the message
     * @param placeholders the placeholders
     * @return the component
     */
    public static Component toComponent(List<String> message, Object... placeholders) {
        if (message == null || message.isEmpty())
            return Component.empty();

        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        List<String> messages = parseAll(message, container);
        Component flatComponent = ComponentUtils.toFlatComponent(messages);
        flatComponent = container.applyUntrusted(flatComponent);
        return flatComponent;
    }

    /**
     * Sends a title to a player.
     *
     * @param player the player.
     * @param title the title to send.
     * @param placeholders the placeholders.
     */
    public static void sendTitle(Player player, Title title, Object... placeholders) {
        Audience audience = ComponentUtils.bukkitAudiences.player(player);
        audience.sendTitlePart(TitlePart.TITLE, toComponent(title.getTitle(), placeholders));
        audience.sendTitlePart(TitlePart.SUBTITLE, toComponent(title.getSubtitle(), placeholders));
        audience.sendTitlePart(TitlePart.TIMES, net.kyori.adventure.title.Title.Times.times(
                Duration.of(title.getIn() * 50L, ChronoUnit.MILLIS),
                Duration.of(title.getStay() * 50L, ChronoUnit.MILLIS),
                Duration.of(title.getOut() * 50L, ChronoUnit.MILLIS)
        ));
    }

    /**
     * Sends an action bar to a player.
     *
     * @param player the player.
     * @param actionBar the action bar to send.
     * @param placeholders the placeholders.
     */
    public static void sendActionBar(Player player, String actionBar, Object... placeholders) {
        Audience audience = ComponentUtils.bukkitAudiences.player(player);
        audience.sendActionBar(toComponent(actionBar, placeholders));
    }

    public static void pass(CommandSender sender, String message, Object... placeholders){
        ComponentUtils.bukkitAudiences.sender(sender).sendMessage(toComponent(message, placeholders));
    }

    public static void pass(CommandSender sender, List<String> message, Object... placeholders){
        ComponentUtils.bukkitAudiences.sender(sender).sendMessage(toComponent(message, placeholders));
    }
}
