package com.golfing8.kcommon.util;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.nms.access.NMSAccess;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.struct.title.Title;
import com.golfing8.kcommon.util.string.StringMacros;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains message utilities used for properly parsing things
 */
@UtilityClass
public final class MS {
    private static final List<Function<String, String>> TRANSFORMERS = Lists.newArrayList();

    static {
        //Color first, always
        TRANSFORMERS.add(str -> ChatColor.translateAlternateColorCodes('&', str));

        //Try hex coloring, if we're on a version which supports it.
        TRANSFORMERS.add(string -> {
            if (KCommon.getInstance() == null || KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_16))
                return RGBUtils.INSTANCE.hexColor(string);
            return string;
        });

        TRANSFORMERS.add(StringMacros.DEFAULT::parse);
    }

    /**
     * Applies all transformers to the given string
     *
     * @param str the string
     * @return the transformed string
     */
    @Contract(pure = true)
    public static @NotNull String applyTransformers(@NotNull String str) {
        for (Function<String, String> func : TRANSFORMERS) {
            str = func.apply(str);
        }
        return str;
    }

    /**
     * Parses all the messages by calling parseSingle for every message.
     *
     * @param messages     the messages to parse
     * @param placeholders the placeholders to use
     * @return the built list
     */
    public static List<String> parseAll(List<String> messages, Object... placeholders) {
        return PlaceholderContainer.compileTrusted(placeholders).applyTrusted(messages).stream().map(MS::applyTransformers).collect(Collectors.toList());
    }

    /**
     * Parses a single string.
     *
     * @param message      the message
     * @param placeholders the placeholders
     * @return the parsed string.
     */
    public static String parseSingle(String message, Object... placeholders) {
        if (message == null)
            return null;

        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        message = container.applyTrusted(Collections.singletonList(message)).get(0);
        message = applyTransformers(message);
        return message;
    }

    /**
     * Converts the message to a component.
     *
     * @param message      the message
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
     * @param message      the message
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
     * Converts the message to a component.
     *
     * @param message      the message
     * @param placeholders the placeholders
     * @return the component
     */
    public static List<Component> toComponentList(List<String> message, Object... placeholders) {
        if (message == null || message.isEmpty())
            return Collections.emptyList();

        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        List<String> messages = parseAll(message, container);
        List<Component> componentList = ComponentUtils.toComponent(messages);
        return componentList.stream().map(container::applyUntrusted).collect(Collectors.toList());
    }

    /**
     * Sends a title to a player.
     *
     * @param player       the player.
     * @param title        the title to send.
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
     * @param player       the player.
     * @param actionBar    the action bar to send.
     * @param placeholders the placeholders.
     */
    public static void sendActionBar(Player player, String actionBar, Object... placeholders) {
        Audience audience = ComponentUtils.bukkitAudiences.player(player);
        audience.sendActionBar(toComponent(actionBar, placeholders));
    }

    /**
     * Passes the message to the given sender
     *
     * @param sender the sender
     * @param message the message
     * @param placeholders the placeholders
     */
    public static void pass(CommandSender sender, String message, Object... placeholders) {
        // Don't send messages to the item capture player.
        if (sender.getName().equals(NMSAccess.ITEM_CAPTURE_NAME))
            return;

        ComponentUtils.bukkitAudiences.sender(sender).sendMessage(toComponent(message, placeholders));
    }

    /**
     * Passes the message to the given receivers
     *
     * @param receivers the receivers of the message
     * @param message the message
     * @param placeholders the placeholders
     */
    public static void pass(Collection<? extends CommandSender> receivers, String message, Object... placeholders) {
        Component component = toComponent(message, placeholders);
        for (CommandSender sender : receivers) {
            // Don't send messages to the item capture player.
            if (sender.getName().equals(NMSAccess.ITEM_CAPTURE_NAME))
                return;

            ComponentUtils.bukkitAudiences.sender(sender).sendMessage(component);
        }
    }

    /**
     * Passes the message to the given sender
     *
     * @param sender the sender
     * @param message the message
     * @param placeholders the placeholders
     */
    public static void pass(CommandSender sender, List<String> message, Object... placeholders) {
        // Don't send messages to the item capture player.
        if (sender.getName().equals(NMSAccess.ITEM_CAPTURE_NAME))
            return;

        ComponentUtils.bukkitAudiences.sender(sender).sendMessage(toComponent(message, placeholders));
    }

    /**
     * Passes the message to the given receivers
     *
     * @param receivers the receivers of the message
     * @param message the message
     * @param placeholders the placeholders
     */
    public static void pass(Collection<? extends CommandSender> receivers, List<String> message, Object... placeholders) {
        Component component = toComponent(message, placeholders);
        for (CommandSender sender : receivers) {
            // Don't send messages to the item capture player.
            if (sender.getName().equals(NMSAccess.ITEM_CAPTURE_NAME))
                return;

            ComponentUtils.bukkitAudiences.sender(sender).sendMessage(component);
        }
    }
}
