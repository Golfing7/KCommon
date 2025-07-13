package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.struct.title.Title;
import com.golfing8.kcommon.util.MS;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An interface that contains methods for sending a message.
 */
public interface MessageContainer {
    /**
     * Gets the message.
     *
     * @return the message.
     */
    Message getMessage();

    default PagedMessage toPagedMessage(Object... placeholders) {
        return new PagedMessage(cloneAndParse(placeholders));
    }

    /**
     * Clones and parses the given placeholders into this message.
     *
     * @param placeholders an argument list of placeholders.
     * @return the cloned message with the replaced placeholders.
     */
    default Message cloneAndParse(Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        List<String> newMessages = null;
        if (getMessage().getMessages() != null) {
            newMessages = MS.parseAll(getMessage().getMessages(), container);
        }

        Title newTitle = null;
        if (getMessage().getTitle() != null) {
            newTitle = new Title(
                    MS.parseSingle(getMessage().getTitle().getTitle(), container),
                    MS.parseSingle(getMessage().getTitle().getSubtitle(), container),
                    getMessage().getTitle().getIn(),
                    getMessage().getTitle().getStay(),
                    getMessage().getTitle().getOut()
            );
        }

        String newActionBar = null;
        if (getMessage().getActionBar() != null) {
            newActionBar = MS.parseSingle(getMessage().getActionBar(), container);
        }

        List<SoundWrapper> newSounds = null;
        if (getMessage().getSounds() != null) {
            newSounds = getMessage().getSounds().stream().map(SoundWrapper::new).collect(Collectors.toList());
        }
        return new Message(newMessages, newSounds, newTitle, newActionBar, getMessage().isPaged(), getMessage().getPageHeight(), MS.parseSingle(getMessage().getPageHeader(), placeholders), MS.parseSingle(getMessage().getPageFooter(), placeholders));
    }

    default void broadcast(Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (getMessage().getMessages() != null) {
            if (getMessage().isPaged()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    send(player, container);
                }
            } else {
                Component component = MS.toComponent(getMessage().getMessages(), container);
                ComponentUtils.bukkitAudiences.all().sendMessage(component);
            }
        }


        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getMessage().getTitle() != null) {
                MS.sendTitle(player, getMessage().getTitle(), container);
            }

            if (getMessage().getActionBar() != null) {
                MS.sendActionBar(player, getMessage().getActionBar(), container);
            }

            if (getMessage().getSounds() != null) {
                getMessage().getSounds().forEach(sound -> {
                    sound.send(player);
                });
            }
        }
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender, Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (getMessage().getMessages() != null) {
            if (getMessage().isPaged()) {
                toPagedMessage(container).displayTo(sender, 1, placeholders);
            } else {
                MS.pass(sender, getMessage().getMessages(), container);
            }
        }

        if (getMessage().getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getMessage().getTitle(), container);
        }

        if (getMessage().getActionBar() != null && sender instanceof Player) {
            MS.sendActionBar((Player) sender, getMessage().getActionBar(), container);
        }

        if (getMessage().getSounds() != null && sender instanceof Player) {
            getMessage().getSounds().forEach(sound -> {
                sound.send((Player) sender);
            });
        }
    }
}
