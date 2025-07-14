package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.title.Title;
import com.google.common.collect.Lists;
import lombok.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;

import javax.annotation.Nullable;
import java.util.*;

/**
 * A message which can represent a string or a list of strings.
 */
@Getter
@Builder
@NoArgsConstructor
public class Message implements MessageContainer {
    /**
     * The message to send to the player, can be a string or list.
     */
    @Nullable
    private List<String> messages;
    @Nullable
    private List<SoundWrapper> sounds;
    /**
     * The title to display to the user, can be null.
     */
    @Nullable
    private Title title;
    /**
     * The message for the player's action bar
     */
    @Nullable
    private String actionBar;
    /**
     * If the message should be paged
     */
    @Setter
    private boolean paged;
    @Setter
    @Builder.Default
    private int pageHeight = PagedMessage.DEFAULT_PAGE_HEIGHT;
    @Setter
    @Builder.Default
    private String pageHeader = PagedMessage.DEFAULT_PAGE_HEADER;
    @Setter
    @Builder.Default
    private String pageFooter = PagedMessage.DEFAULT_PAGE_FOOTER;

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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Message(@Nullable Object message) {
        this();
        if (message == null) {
            return;
        }

        if (message instanceof String) {
            this.messages = Lists.newArrayList(message.toString());
        } else if (message instanceof List) {
            this.messages = new ArrayList<>();
            ((List<?>) message).forEach(object -> this.messages.add(object.toString()));
        } else if (message instanceof ConfigurationSection) { //In this case the player might be defining a title too.
            ConfigurationSection section = (ConfigurationSection) message;
            //Check for the title
            this.title = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "title"),
                    new FieldType(Title.class));

            if (section.contains("message")) {
                Object oMsg = section.get("message");
                //Get the actual message.
                if (oMsg instanceof String) {
                    this.messages = Lists.newArrayList(oMsg.toString());
                } else if (oMsg instanceof List) {
                    this.messages = new ArrayList<>();
                    ((List<?>) oMsg).forEach(object -> this.messages.add(object.toString()));
                } else {
                    throw new IllegalArgumentException(String.format("Message %s is not a string or a list!", oMsg));
                }
            }

            // Read the action bar.
            this.actionBar = section.getString("actionbar");

            //Load the sounds.
            this.sounds = new ArrayList<>();
            if (section.contains("sounds")) {
                for (String soundKey : section.getConfigurationSection("sounds").getKeys(false)) {
                    SoundWrapper fromType = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "sounds." + soundKey),
                            new FieldType(SoundWrapper.class));
                    this.sounds.add(fromType);
                }
            }

            if (section.getBoolean("paged")) {
                this.paged = section.getBoolean("paged");
                this.pageHeight = (Integer) section.get("page-height", PagedMessage.DEFAULT_PAGE_HEIGHT);
                this.pageHeader = (String) section.get("page-header", PagedMessage.DEFAULT_PAGE_HEADER);
                this.pageFooter = (String) section.get("page-footer", PagedMessage.DEFAULT_PAGE_FOOTER);
            }
        } else if (message instanceof Map) { //In this case the player might be defining a title too.
            Map section = (Map) message;
            //Check for the title
            this.title = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofNullable(section.get("title")), Title.class);

            // Read the action bar.
            this.actionBar = (String) section.get("actionbar");

            if (section.containsKey("message")) {
                //Get the actual message.
                Object oMsg = section.get("message");
                if (oMsg instanceof String) {
                    this.messages = Lists.newArrayList(oMsg.toString());
                } else if (oMsg instanceof List) {
                    this.messages = new ArrayList<>();
                    ((List<?>) oMsg).forEach(object -> this.messages.add(object.toString()));
                } else {
                    throw new IllegalArgumentException(String.format("Message %s is not a string or a list!", oMsg));
                }
            }

            //Load the sounds.
            this.sounds = new ArrayList<>();
            if (section.containsKey("sounds")) {
                var soundSection = (Map<String, Object>) section.get("sounds");
                for (var entry : soundSection.entrySet()) {
                    this.sounds.add(ConfigTypeRegistry.getFromType(ConfigPrimitive.of(entry.getValue()), SoundWrapper.class));
                }
            }

            if (section.containsKey("paged")) {
                this.paged = (Boolean) section.get("paged");
                this.pageHeight = (Integer) section.getOrDefault("page-height", PagedMessage.DEFAULT_PAGE_HEIGHT);
                this.pageHeader = (String) section.getOrDefault("page-header", PagedMessage.DEFAULT_PAGE_HEADER);
                this.pageFooter = (String) section.getOrDefault("page-footer", PagedMessage.DEFAULT_PAGE_FOOTER);
            }
        } else {
            throw new IllegalArgumentException(String.format("Message %s is not a string or a list!", message));
        }
    }

    public Message(@Nullable List<String> messages, @Nullable List<SoundWrapper> sounds, @Nullable Title title, @Nullable String actionBar) {
        this();
        this.messages = messages == null ? Collections.emptyList() : messages;
        this.sounds = sounds == null ? Collections.emptyList() : sounds;
        this.title = title;
        this.actionBar = actionBar;
    }

    public Message(@Nullable List<String> messages, @Nullable List<SoundWrapper> sounds, @Nullable Title title, @Nullable String actionBar,
                   boolean paged, int pageHeight, String pageHeader, String pageFooter) {
        this();
        this.messages = messages == null ? Collections.emptyList() : messages;
        this.sounds = sounds == null ? Collections.emptyList() : sounds;
        this.title = title;
        this.actionBar = actionBar;
        this.paged = paged;
        this.pageHeight = pageHeight;
        this.pageHeader = pageHeader;
        this.pageFooter = pageFooter;
    }

    /**
     * Checks if this message should be considered 'simple'.
     * A simple message has no title or sounds attached to it.
     *
     * @return if this message only has simple text messages.
     */
    public boolean isSimple() {
        return (this.sounds == null || this.sounds.isEmpty()) &&
                (this.title == null) &&
                (this.actionBar == null) &&
                !this.isPaged() &&
                Objects.equals(PagedMessage.DEFAULT_PAGE_HEIGHT, pageHeight) &&
                Objects.equals(PagedMessage.DEFAULT_PAGE_HEADER, pageHeader) &&
                Objects.equals(PagedMessage.DEFAULT_PAGE_FOOTER, pageFooter);
    }

    /**
     * Checks if this message is empty, meaning that if any variant of {@code send} is called, nothing happens.
     *
     * @return if this message is empty.
     */
    public boolean isEmpty() {
        return this.isSimple() && (this.messages == null || this.messages.isEmpty());
    }

    @Override
    public Message getMessage() {
        return this;
    }
}
