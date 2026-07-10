package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.struct.title.Title;
import com.golfing8.kcommon.util.MS;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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

    /**
     * Converts this container to a paged message
     *
     * @param placeholders the placeholders
     * @return the new paged message
     */
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

    /**
     * Appends the given message to this message returns the result as a new message
     * <p>
     * This method will always return a new copy of a message
     * </p>
     *
     * @param message the message
     * @return the new wrapper
     */
    default Message append(@Nullable MessageContainer message) {
        return append(message, " ");
    }

    /**
     * Appends the given message to this message returns the result as a new message
     * <p>
     * This method will always return a new copy of a message
     * </p>
     *
     * @param message the message
     * @param separator the separator to apply between the messages
     * @return the new wrapper
     */
    default Message append(@Nullable MessageContainer message, @Nullable String separator) {
        return append(message, AppendMode.END_OF_LINE, separator);
    }

    /**
     * Appends the given message to this message returns the result as a new message
     * <p>
     * This method will always return a new copy of a message
     * </p>
     *
     * @param message the message
     * @param appendMode the appending mode
     * @param separator the separator to apply between the messages
     * @return the new wrapper
     */
    default Message append(@Nullable MessageContainer message, @NotNull AppendMode appendMode, @Nullable String separator) {
        if (message == null) {
            return new Message(this);
        }

        List<String> newMessages = appendMessages(message, appendMode, separator);
        List<SoundWrapper> newSounds = new ArrayList<>();
        if (getMessage().getSounds() != null) {
            newSounds.addAll(getMessage().getSounds());
        }
        if (message.getMessage().getSounds() != null) {
            newSounds.addAll(message.getMessage().getSounds());
        }

        String newActionBar = StringUtils.join(new String[] {getMessage().getActionBar(), message.getMessage().getActionBar()}, ' ');
        Title newTitle;
        if (getMessage().getTitle() == null) {
            newTitle = message.getMessage().getTitle();
        } else if (message.getMessage().getTitle() == null) {
            newTitle = getMessage().getTitle();
        } else {
            String newTitleString = StringUtils.join(new String[] {
                    getMessage().getTitle().getTitle(),
                    message.getMessage().getTitle().getTitle()
            }, separator);
            String newSubtitleString = StringUtils.join(new String[] {
                    getMessage().getTitle().getSubtitle(),
                    message.getMessage().getTitle().getSubtitle()
            }, separator);
            newTitle = new Title(newTitleString, newSubtitleString, getMessage().getTitle().getIn(), getMessage().getTitle().getStay(), getMessage().getTitle().getOut());
        }
        return new Message(newMessages, newSounds, newTitle, newActionBar);
    }

    /**
     * Appends the messages only for this and that message container
     *
     * @param message the message
     * @param appendMode the append mode
     * @param separator the separator to use
     * @return the appended messages
     */
    default @Nullable List<String> appendMessages(@NotNull MessageContainer message, @NotNull AppendMode appendMode, @Nullable String separator) {
        List<String> newMessages;
        List<String> messages = this.getMessage().getMessages();
        List<String> appendedMessages = message.getMessage().getMessages();
        if (messages == null) {
            newMessages = appendedMessages;
        } else if (appendedMessages == null) {
            newMessages = messages;
        } else {
            String usedSeparator = separator == null ? "" : separator;
            if (appendMode == AppendMode.END_OF_LINE) {
                List<String> newLines = new ArrayList<>();
                int size = Math.min(messages.size(), appendedMessages.size());
                for (int i = 0; i < size; i++) {
                    newLines.add(messages.get(i) + usedSeparator + appendedMessages.get(i));
                }
                newMessages = newLines;
            } else if (appendMode == AppendMode.INTERLEAVE) {
                List<String> newLines = new ArrayList<>();
                int size = Math.max(messages.size(), appendedMessages.size());
                for (int i = 0; i < size; i++) {
                    if (i < messages.size()) {
                        newLines.add(messages.get(i));
                    }
                    if (i < appendedMessages.size()) {
                        newLines.add(appendedMessages.get(i));
                    }
                }
                newMessages = newLines;
            } else if (appendMode == AppendMode.AFTER) {
                newMessages = new ArrayList<>(messages);
                newMessages.addAll(appendedMessages);
            } else {
                throw new IllegalArgumentException("Unknown append mode: " + appendMode);
            }
        }
        return newMessages;
    }

    /**
     * Broadcasts the message with the given placeholders
     *
     * @param placeholders the placeholders
     */
    default void broadcast(Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (getMessage().getMessages() != null && !getMessage().getMessages().isEmpty()) {
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
     * @param sender       the sender.
     * @param placeholders the placeholders.
     */
    default void send(CommandSender sender, Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (getMessage().getMessages() != null && !getMessage().getMessages().isEmpty()) {
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

    /**
     * Sends the message to the collection of command senders
     *
     * @param receivers the receivers
     * @param placeholders the placeholders
     */
    default void send(Collection<? extends CommandSender> receivers, Object... placeholders) {
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (getMessage().getMessages() != null && !getMessage().getMessages().isEmpty()) {
            if (getMessage().isPaged()) {
                // Paged messages are tracked individually, this hack is necessary.
                for (CommandSender sender : receivers) {
                    toPagedMessage(container).displayTo(sender, 1, placeholders);
                }
            } else {
                MS.pass(receivers, getMessage().getMessages(), container);
            }
        }

        for (CommandSender sender : receivers) {
            if (!(sender instanceof Player))
                continue;

            if (getMessage().getTitle() != null) {
                MS.sendTitle((Player) sender, getMessage().getTitle(), container);
            }

            if (getMessage().getActionBar() != null) {
                MS.sendActionBar((Player) sender, getMessage().getActionBar(), container);
            }

            if (getMessage().getSounds() != null) {
                getMessage().getSounds().forEach(sound -> {
                    sound.send((Player) sender);
                });
            }
        }
    }

    /**
     * The type of append mode to use for appending messages.
     * <p>
     * Note that all action bars and titles are always <code>END_OF_LINE</code>.
     * </p>
     */
    enum AppendMode {
        /**
         * All messages will be appended after all other messages from message 1.
         * <ol>
         *     <li>Message1: line1, line2, line3</li>
         *     <li>Message2: line4, line5, line6</li>
         * </ol>
         * Result:
         * <ol>
         *     <li>line1</li>
         *     <li>line2</li>
         *     <li>line3</li>
         *     <li>line4</li>
         *     <li>line5</li>
         *     <li>line6</li>
         * </ol>
         */
        AFTER,
        /**
         * All messages will be interleaved with the other message.
         * <ol>
         *     <li>Message1: line1, line2, line3</li>
         *     <li>Message2: line4, line5, line6</li>
         * </ol>
         * Result:
         * <ol>
         *     <li>line1</li>
         *     <li>line4</li>
         *     <li>line2</li>
         *     <li>line5</li>
         *     <li>line3</li>
         *     <li>line6</li>
         * </ol>
         */
        INTERLEAVE,
        /**
         * All messages will be appended at the end of the line with a space separator between the messages
         * <ol>
         *     <li>Message1: line1, line2, line3</li>
         *     <li>Message2: line4, line5, line6</li>
         * </ol>
         * Result:
         * <ol>
         *     <li>line1 line4</li>
         *     <li>line2 line5</li>
         *     <li>line3 line6</li>
         * </ol>
         */
        END_OF_LINE,
    }
}
