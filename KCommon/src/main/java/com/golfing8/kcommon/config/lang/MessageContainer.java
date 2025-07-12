package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.title.Title;
import com.golfing8.kcommon.util.MS;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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

    default PagedMessage toPagedMessage(Collection<Placeholder> placeholders, Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        return new PagedMessage(cloneAndParse(placeholders, multiLinePlaceholders));
    }

    /**
     * Clones and parses the given placeholders into this message.
     *
     * @param placeholders an argument list of placeholders.
     * @return the cloned message with the replaced placeholders.
     */
    default Message cloneAndParse(Object... placeholders) {
        List<Placeholder> compiled = Placeholder.compileCurly(placeholders);
        return cloneAndParse(compiled, Collections.emptyList());
    }

    /**
     * Clones and parses the given placeholders into this message.
     *
     * @param placeholders the placeholders.
     * @param multiLinePlaceholders the multi-line placeholders.
     * @return the cloned message with the replaced placeholders.
     */
    default Message cloneAndParse(Collection<Placeholder> placeholders, Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        List<String> newMessages = null;
        if (messages != null) {
            newMessages = MS.parseAllMulti(MS.parseAll(messages, placeholders), multiLinePlaceholders);
        }

        Title newTitle = null;
        if (title != null) {
            newTitle = new Title(
                    MS.parseSingle(title.getTitle(), placeholders),
                    MS.parseSingle(title.getSubtitle(), placeholders),
                    title.getIn(),
                    title.getStay(),
                    title.getOut()
            );
        }

        String newActionBar = null;
        if (actionBar != null) {
            newActionBar = MS.parseSingle(actionBar, placeholders);
        }

        List<SoundWrapper> newSounds = null;
        if (sounds != null) {
            newSounds = sounds.stream().map(SoundWrapper::new).collect(Collectors.toList());
        }
        return new Message(newMessages, newSounds, newTitle, newActionBar, paged, pageHeight, MS.parseSingle(pageHeader, placeholders), MS.parseSingle(pageFooter, placeholders));
    }

    default void broadcast() {
        broadcast(null, null);
    }

    default void broadcast(Object... placeholders) {
        this.broadcast(Placeholder.compileCurly(placeholders), null);
    }

    default void broadcast(@Nullable Collection<Placeholder> placeholders,
                          @Nullable Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        if (this.getMessages() != null) {
            if (paged) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    send(player, placeholders, multiLinePlaceholders);
                }
            } else {
                List<String> parsed = MS.parseAllMulti(MS.parseAll(this.getMessages(), placeholders == null ? Collections.emptyList() : placeholders), multiLinePlaceholders == null ? Collections.emptyList() : multiLinePlaceholders);
                parsed.forEach(line -> NMS.getTheNMS().broadcastComponent(ComponentUtils.toComponent(line)));
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (getTitle() != null) {
                MS.sendTitle(player, getTitle(), placeholders);
            }

            if (getActionBar() != null) {
                ComponentUtils.bukkitAudiences.player(player).sendActionBar(MS.toComponent(actionBar, placeholders));
            }

            if (getSounds() != null) {
                getSounds().forEach(sound -> {
                    sound.send(player);
                });
            }
        }
    }

    /**
     * An overload for the sake of making Kotlin easier to interop.
     *
     * @param sender the sender.
     */
    default void send(CommandSender sender) {
        this.send(sender, new Object[0]);
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender, Object... placeholders) {
        if (getMessages() != null) {
            if (paged) {
                toPagedMessage(placeholders).displayTo(sender);
            } else {
                getMessages().forEach(string -> MS.pass(sender, string, placeholders));
            }
        }

        if (getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getTitle(), placeholders);
        }

        if (getActionBar() != null && sender instanceof Player) {
            ComponentUtils.bukkitAudiences.player((Player) sender).sendActionBar(MS.toComponent(actionBar, placeholders));
        }

        if (getSounds() != null && sender instanceof Player) {
            getSounds().forEach(sound -> {
                sound.send((Player) sender);
            });
        }
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender, Placeholder... placeholders) {
        this.send(sender, Arrays.asList(placeholders));
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender, Collection<Placeholder> placeholders) {
        this.send(sender, placeholders, Collections.emptyList());
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender,
                     @Nullable Collection<Placeholder> placeholders,
                     @Nullable Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        if (getMessages() != null) {
            if (paged) {
                toPagedMessage(placeholders, multiLinePlaceholders).displayTo(sender);
            } else {
                MS.parseAllMulti(MS.parseAll(getMessages(), placeholders == null ? Collections.emptyList() : placeholders),
                                multiLinePlaceholders == null ? Collections.emptyList() : multiLinePlaceholders)
                        .forEach(string -> MS.pass(sender, string));
            }
        }

        if (getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getTitle(), placeholders);
        }

        if (getActionBar() != null && sender instanceof Player) {
            ComponentUtils.bukkitAudiences.player((Player) sender).sendActionBar(MS.toComponent(actionBar, placeholders));
        }

        if (getSounds() != null && sender instanceof Player) {
            getSounds().forEach(sound -> sound.send((Player) sender));
        }
    }
}
