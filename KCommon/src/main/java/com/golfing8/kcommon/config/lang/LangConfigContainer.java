package com.golfing8.kcommon.config.lang;

import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import com.golfing8.kcommon.util.Reflection;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a class that contains a language config and can allow sending of config messages.
 * <p />
 * Note that an implementation of this interface does NOT mean that its lang config is unique!
 * Commands and modules SHARE one language config!
 */
public interface LangConfigContainer {

    /**
     * Gets the prefix to use in this lang config container implementation.
     *
     * @return the prefix.
     */
    default String getPrefix() {
        return "";
    }

    /**
     * Gets the formatted prefix with the given postfix.
     *
     * @param postfix the postfix to append.
     * @return the formatted prefix.
     */
    default String getFormattedPrefix(String postfix) {
        String prefix = getPrefix();
        if(prefix.endsWith("."))
            prefix = prefix.substring(0, prefix.length() - 1);

        return (prefix.isEmpty() ? prefix : prefix + ".") + postfix;
    }


    /**
     * Sends a config message to the player, if it doesn't exist in the config it is added.
     * <p />
     * This method should be avoided in favor of using {@link LangConfig#addLanguageConstant(String, List)}.
     * It is provided for the sake of convenience.
     *
     * @param sender the receiver of the message.
     * @param key the key of the message.
     * @param defaultMsg the default message.
     * @param placeholders the placeholders to apply to the message.
     */
    default void sendDefaultMessage(CommandSender sender, String key, String defaultMsg, Placeholder... placeholders) {
        this.sendDefaultMessage(sender, key, new Message(defaultMsg), placeholders);
    }

    /**
     * Sends a config message to the player, if it doesn't exist in the config it is added.
     * <p />
     * This method should be avoided in favor of using {@link LangConfig#addLanguageConstant(String, List)}.
     * It is provided for the sake of convenience.
     *
     * @param sender the receiver of the message.
     * @param key the key of the message.
     * @param defaultMsg the default message.
     * @param placeholders the placeholders to apply to the message.
     */
    default void sendDefaultMessage(CommandSender sender, String key, Message defaultMsg, Placeholder... placeholders) {
        String formatted = getFormattedPrefix(key);
        Message message = getLangConfig().getMessage(formatted);
        if(message == null) {
            //Define the message.
            getLangConfig().addLanguageConstant(formatted, defaultMsg);
            message = getLangConfig().getMessage(formatted);
            getLangConfig().save();
        }
        MS.parseAll(message.getMessages(), placeholders)
                .forEach(string -> MS.pass(sender, string));
        if(message.getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, message.getTitle());
        }
    }

    /**
     * Sends a message from the config message to the given command sender.
     *
     * @param sender the receiver of the message.
     * @param key the key of the message.
     * @param placeholders the placeholders to apply to the message
     * @param multiLinePlaceholders the multi-line placeholders to apply.
     */
    default void sendConfigMessage(CommandSender sender,
                                   String key,
                                   @Nullable Collection<Placeholder> placeholders,
                                   @Nullable Collection<MultiLinePlaceholder> multiLinePlaceholders) {
        String formatted = getFormattedPrefix(key);
        Message message = getLangConfig().getMessage(formatted);
        Preconditions.checkNotNull(message, String.format("Tried to send message with key %s from config %s but it does not exist!", formatted, this.getLangConfig().getConfigPath()));

        message.send(sender, placeholders);
        MS.parseAllMulti(MS.parseAll(message.getMessages(), placeholders == null ? Collections.emptyList() : placeholders),
                        multiLinePlaceholders == null ? Collections.emptyList() : multiLinePlaceholders)
                .forEach(string -> MS.pass(sender, string));
        //Then we need to parse all the titles.
        if(message.getTitle() != null && sender instanceof Player) {
            MS.sendTitle((Player) sender, message.getTitle());
        }
    }

    /**
     * Sends a message from the config message to the given command sender.
     *
     * @param sender the receiver of the message.
     * @param key the key of the message.
     * @param placeholders the placeholders to apply to the message
     */
    default void sendConfigMessage(CommandSender sender, String key, Placeholder... placeholders) {
        String formatted = getFormattedPrefix(key);
        Message message = getLangConfig().getMessage(formatted);
        Preconditions.checkNotNull(message, String.format("Tried to send message with key %s from config %s but it does not exist!", formatted, this.getLangConfig().getConfigPath()));
        message.send(sender, placeholders);
    }

    /**
     * Sends a message from the config message to the given command sender.
     *
     * @param sender the receiver of the message.
     * @param key the key of the message.
     * @param placeholders the placeholders to apply to the message
     */
    default void sendConfigMessage(CommandSender sender, String key, Object... placeholders) {
        String formatted = getFormattedPrefix(key);
        Message message = getLangConfig().getMessage(formatted);
        Preconditions.checkNotNull(message, String.format("Tried to send message with key %s from config %s but it does not exist!", formatted, this.getLangConfig().getConfigPath()));

        message.send(sender, placeholders);
    }

    /**
     * Adds a language constant with the given key and the value.
     *
     * @param key the key of the value.
     * @param value the actual value.
     */
    default void addLanguageConstant(String key, String... value) {
        addLanguageConstant(key, Arrays.asList(value));
    }

    /**
     * Adds a language constant with the given key and the value.
     *
     * @param key the key of the value.
     * @param value the actual value.
     * @return the message that is actually in the config.
     */
    default Message addLanguageConstant(String key, List<String> value) {
        return addLanguageConstant(key, Message.builder().messages(value).build());
    }

    /**
     * Adds a language constant with the given key and the value.
     *
     * @param key the key of the value.
     * @param value the actual value.
     * @return the message that is actually in the config.
     */
    default Message addLanguageConstant(String key, Message value) {
        String formatted = getFormattedPrefix(key);
        getLangConfig().addLanguageConstant(formatted, value);
        return getLangConfig().getMessage(formatted);
    }

    /**
     * Loads the language enums attached to this class.
     */
    default void loadLangEnums() {
        Set<Class<?>> nestedClasses = Reflection.getAllNestedClasses(getClass());
        for (Class<?> clazz : nestedClasses) {
            // Check that we're operating on an enum and a LangEnum instance.
            if (!LangEnum.class.isAssignableFrom(clazz) || !clazz.isEnum())
                continue;

            LangEnum[] enums = (LangEnum[]) clazz.getEnumConstants();
            for (LangEnum langEnum : enums) {
                Enum<?> enumValue = (Enum<?>) langEnum;
                String yamlPath = StringUtil.enumToYaml(enumValue.name());
                String formattedPath = getFormattedPrefix(yamlPath);
                addLanguageConstant(formattedPath, new Message(langEnum.getMessage()));
                langEnum.setMessage(getLangConfig().getMessage(formattedPath));
            }
        }
    }

    /**
     * If the LangConfig managing this container should reflectively load language enums for the implementor of this interface.
     *
     * @return true if this supports lang enums.
     */
    default boolean supportsLangEnums() {
        return true;
    }

    /**
     * Gets the lang config backing this container.
     *
     * @return the language config.
     */
    LangConfig getLangConfig();
}
