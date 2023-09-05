package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.FieldType;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.title.Title;
import com.golfing8.kcommon.util.MS;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A message which can represent a string or a list of strings.
 */
@Builder
public class Message {
    /**
     * The message to send to the player, can be a string or list.
     */
    @Getter @Nullable
    private final List<String> messages;
    @Getter @Nullable
    private List<SoundWrapper> sounds;
    /**
     * The title to display to the user, can be null.
     */
    @Getter @Nullable
    private Title title;
    /** The message for the player's action bar */
    @Getter @Nullable
    private String actionBar;

    /**
     * Constructs a message depending on the type given.
     * <ul>
     * <li>If {@link String}, simply constructs a one line message.</li>
     * <li>If {@link List}, constructs a multi-line message.</li>
     * <li>If {@link MemorySection}, constructs a compound message based upon the definition in the config..</li>
     * </ul>
     *
     * @param message the message to load from.
     */
    public Message(Object message) {
        if(message instanceof String) {
            this.messages = Lists.newArrayList(message.toString());
        }else if(message instanceof List) {
            this.messages = new ArrayList<>();
            ((List<?>) message).forEach(object -> this.messages.add(object.toString()));
        }else if(message instanceof MemorySection) { //In this case the player might be defining a title too.
            MemorySection section = (MemorySection) message;
            //Check for the title
            this.title = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "title"),
                    new FieldType(Title.class));
            Object oMsg = section.get("message");

            // Read the action bar.
            this.actionBar = section.getString("actionbar");

            //Get the actual message.
            if(oMsg instanceof String) {
                this.messages = Lists.newArrayList(oMsg.toString());
            }else if(oMsg instanceof List) {
                this.messages = new ArrayList<>();
                ((List<?>) oMsg).forEach(object -> this.messages.add(object.toString()));
            }else {
                throw new IllegalArgumentException(String.format("Message %s is not a string or a list!", oMsg));
            }

            //Load the sounds.
            this.sounds = new ArrayList<>();
            if(section.contains("sounds")) {
                for(String soundKey : section.getConfigurationSection("sounds").getKeys(false)) {
                    SoundWrapper fromType = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "sounds." + soundKey),
                            new FieldType(SoundWrapper.class));
                    System.out.println(fromType);
                    this.sounds.add(fromType);
                }
            }
        }else {
            throw new IllegalArgumentException(String.format("Message %s is not a string or a list!", message.toString()));
        }
    }

    public Message(@Nullable List<String> messages, @Nullable List<SoundWrapper> sounds, @Nullable Title title, @Nullable String actionBar) {
        this.messages = messages == null ? Collections.emptyList() : messages;
        this.sounds = sounds == null ? Collections.emptyList() : sounds;
        this.title = title;
        this.actionBar = actionBar;
    }

    /**
     * Checks if this message should be considered 'simple'.
     * A simple message has no title or sounds attached to it.
     *
     * @return if this message only has simple text messages.
     */
    public boolean isSimple() {
        return (this.sounds == null || this.sounds.isEmpty()) && (this.title == null);
    }

    /**
     * Sends this message to the given player with the placeholders.
     *
     * @param sender the sender.
     * @param placeholders the placeholders.
     */
    public void send(CommandSender sender, Object... placeholders) {
        if (getMessages() != null) {
            getMessages().forEach(string -> MS.pass(sender, string, placeholders));
        }

        if (getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getTitle());
        }

        if (getActionBar() != null && sender instanceof Player) {
            NMS.getTheNMS().sendActionBar((Player) sender, MS.parseSingle(actionBar, placeholders));
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
    public void send(CommandSender sender, Placeholder... placeholders) {
        if (getMessages() != null) {
            MS.parseAll(getMessages(), placeholders)
                    .forEach(string -> MS.pass(sender, string));
        }

        if (getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getTitle());
        }

        if (getActionBar() != null && sender instanceof Player) {
            NMS.getTheNMS().sendActionBar((Player) sender, MS.parseSingle(actionBar, placeholders));
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
    public void send(CommandSender sender,
                     @Nullable Collection<Placeholder> placeholders,
                     @Nullable Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        if (getMessages() != null) {
            MS.parseAllMulti(MS.parseAll(getMessages(), placeholders == null ? Collections.emptyList() : placeholders),
                            multiLinePlaceholders == null ? Collections.emptyList() : multiLinePlaceholders)
                    .forEach(string -> MS.pass(sender, string));
        }

        if (getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, getTitle(), placeholders);
        }

        if (getActionBar() != null && sender instanceof Player) {
            NMS.getTheNMS().sendActionBar((Player) sender, MS.parseSingle(actionBar, placeholders));
        }

        if (getSounds() != null && sender instanceof Player) {
            getSounds().forEach(sound -> sound.send((Player) sender));
        }
    }
}
