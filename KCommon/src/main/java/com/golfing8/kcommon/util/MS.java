package com.golfing8.kcommon.util;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.title.Title;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains message utilities used for properly parsing things
 */
@UtilityClass
public final class MS {
    private static final String SPLIT_STRING_SEQUENCE = " \\\\n ";

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

        //Check if the player is wanting to send a title.
        TRANSFORMERS.add(new Function<String, String>() {
            @Override
            public String apply(String s) {
                if(!s.startsWith("title"))
                    return s;
                return s;
            }
        });

        //Center message
        TRANSFORMERS.add(s -> s.startsWith("center::") ? StringUtil.centerMessage(s.substring(8)) : s);
    }

    /**
     * Parses a single message and returns it
     *
     * @param message the message to parse
     * @param placeholders the placeholders to apply.
     * @return the parsed message.
     */
    public static String parseSingle(String message, Placeholder... placeholders) {
        if (message == null)
            return null;

        for(Placeholder placeholder : placeholders) {
            message = message.replace(placeholder.getLabel(), placeholder.getValue());
        }

        for(Function<String, String> func : TRANSFORMERS){
            message = func.apply(message);
        }
        return message;
    }

    /**
     * Parses a single message and returns it
     *
     * @param message the message to parse
     * @param placeholders the placeholders to apply.
     * @return the parsed message.
     */
    public static String parseSingle(String message, Collection<Placeholder> placeholders) {
        if(message == null)
            return null;

        for(Placeholder placeholder : placeholders) {
            message = message.replace(placeholder.getLabel(), placeholder.getValue());
        }

        for(Function<String, String> func : TRANSFORMERS){
            message = func.apply(message);
        }
        return message;
    }

    /**
     * Parses all the messages by calling parseSingle for every message.
     *
     * @param messages the messages to parse
     * @param placeholders the placeholders to use
     * @return the built list
     */
    public static List<String> parseAll(List<String> messages, Placeholder... placeholders) {
        return parseAll(messages, Arrays.asList(placeholders));
    }

    /**
     * Parses all the messages by calling parseSingle for every message.
     *
     * @param messages the messages to parse
     * @param placeholders the placeholders to use
     * @return the built list
     */
    public static List<String> parseAll(List<String> messages, Collection<Placeholder> placeholders) {
        List<String> toReturn = new ArrayList<>();
        messages.forEach(string -> toReturn.add(parseSingle(string, placeholders)));
        return toReturn;
    }

    /**
     * Parses all the messages in the given list and returns the built list of strings
     *
     * @param messages the messages to parse
     * @param placeholders the multi-line placeholders to apply
     * @return the built list
     */
    public static List<String> parseAllMulti(List<String> messages, Collection<MultiLinePlaceholder> placeholders) {
        List<String> toReturn = new ArrayList<>(messages);

        //Loop over all the messages and parse them one at a time
        for (int i = 0; i < toReturn.size(); i++) {
            String line = toReturn.get(i);
            for(MultiLinePlaceholder placeholder : placeholders) {
                if(!line.contains(placeholder.getLabel()))
                    continue;

                //Get the replacements and check if its empty
                List<String> replacement = placeholder.getReplacement();
                if(replacement.isEmpty()) {
                    toReturn.remove(i--);
                    break;
                }

                //Then start replacing them.
                toReturn.set(i, line.replace(placeholder.getLabel(), replacement.get(0)));
                for (int j = 1; j < replacement.size(); j++) {
                    toReturn.add(i + j, line.replace(placeholder.getLabel(), replacement.get(j)));
                }
                break;
            }
        }
        return toReturn;
    }

    /**
     * Parses all the messages in the given list and returns the built list of strings
     *
     * @param messages the messages to parse
     * @param placeholders the multi-line placeholders to apply
     * @return the built list
     */
    public static List<String> parseAllMulti(List<String> messages, MultiLinePlaceholder... placeholders) {
        return parseAllMulti(messages, Arrays.asList(placeholders));
    }

    public static String parseSingle(String message, Object... placeholders){
        if(message == null)
            return null;

        message = Placeholders.parseFully(message, placeholders);

        for(Function<String, String> func : TRANSFORMERS){
            message = func.apply(message);
        }
        return message;
    }

    public static List<String> p(String message, Object... placeholders){
        String[] split = split(message);

        List<String> finished = Lists.newArrayList();

        mainString: for(String string : split){
            string = Placeholders.parseFully(string, placeholders);

            for(Function<String, String> func : TRANSFORMERS){
                String apply = func.apply(string);
                if(apply == null)
                    continue mainString;

                string = apply;
            }

            finished.add(string);
        }
        return finished;
    }

    /**
     * Sends a title to a player.
     *
     * @param player the player.
     * @param title the title to send.
     * @param placeholders the placeholders.
     */
    public static void sendTitle(Player player, Title title, Object... placeholders) {
        NMS.getTheNMS().sendTitle(player,
                MS.parseSingle(title.getTitle(), placeholders),
                MS.parseSingle(title.getSubtitle(), placeholders),
                title.getIn(),
                title.getStay(),
                title.getOut());
    }

    /**
     * Sends a title to a player.
     *
     * @param player the player.
     * @param title the title to send.
     * @param placeholders the placeholders.
     */
    public static void sendTitle(Player player, Title title, Placeholder... placeholders) {
        NMS.getTheNMS().sendTitle(player,
                MS.parseSingle(title.getTitle(), placeholders),
                MS.parseSingle(title.getSubtitle(), placeholders),
                title.getIn(),
                title.getStay(),
                title.getOut());
    }

    /**
     * Sends a title to a player.
     *
     * @param player the player.
     * @param title the title to send.
     * @param placeholders the placeholders.
     */
    public static void sendTitle(Player player, Title title, Collection<Object> placeholders) {
        Object[] objects = placeholders == null ? null : placeholders.toArray(new Object[0]);
        NMS.getTheNMS().sendTitle(player,
                MS.parseSingle(title.getTitle(), objects),
                MS.parseSingle(title.getSubtitle(), objects),
                title.getIn(),
                title.getStay(),
                title.getOut());
    }

    public static void pass(CommandSender sender, String message, Object... placeholders){
        List<String> messages = p(message, placeholders);

        messages.forEach(m -> NMS.getTheNMS().sendMiniMessage(sender, m));
    }

    private static String[] split(String message){
        return message == null ? new String[0] : message.split(SPLIT_STRING_SEQUENCE);
    }
}
