package com.golfing8.kcommon.util;

import com.golfing8.kcommon.nms.access.NMSAccess;
import com.golfing8.kcommon.struct.title.Title;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.Bukkit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

/**
 * Contains message utilities used for properly parsing things
 */
public final class MS extends MSCore {
    private MS() {
    }

    private static BukkitAudiences audiences;

    private static synchronized BukkitAudiences audiences() {
        if (audiences == null)
            audiences = BukkitAudiences.create(Bukkit.getPluginManager().getPlugin("KCommon"));
        return audiences;
    }

    /**
     * Sends a title to a player.
     *
     * @param player       the player.
     * @param title        the title to send.
     * @param placeholders the placeholders.
     */
    public static void sendTitle(Player player, Title title, Object... placeholders) {
        Audience audience = audience(player);
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
        Audience audience = audience(player);
        audience.sendActionBar(toComponent(actionBar, placeholders));
    }

    /**
     * Gets the audience titles and action bars go through. Paper's players already are adventure audiences, so the
     * server builds those packets itself. adventure-platform builds them by reflection, which lags behind new versions:
     * on 26.2 it sent action bars with a null component, which the server then failed to encode.
     *
     * @param sender the command sender.
     * @return the audience.
     */
    public static Audience audience(CommandSender sender) {
        return sender instanceof Audience ? (Audience) sender : audiences().sender(sender);
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

        audience(sender).sendMessage(toComponent(message, placeholders));
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

            audience(sender).sendMessage(component);
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

        audience(sender).sendMessage(toComponent(message, placeholders));
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

            audience(sender).sendMessage(component);
        }
    }
}
