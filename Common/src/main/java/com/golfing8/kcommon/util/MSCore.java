package com.golfing8.kcommon.util;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.golfing8.kcommon.util.string.StringMacros;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Platform independent part of each platform's {@code MS}.
 */
public class MSCore {
    protected MSCore() {
    }

    private static final List<Function<String, String>> TRANSFORMERS = Lists.newArrayList();

    static {
        //Color first, always
        TRANSFORMERS.add(str -> StringUtilCore.translateAlternateColorCodes('&', str));

        //Try hex coloring, if we're on a version which supports it.
        //Legacy '&' codes were already translated by the transformer above, so we can skip
        //RGBUtils re-running that same full-string translation pass a second time.
        TRANSFORMERS.add(string -> {
            if (!Platform.isSet() || Platform.get().supportsHexColors())
                return RGBUtils.INSTANCE.hexColorLegacyAlreadyTranslated(string);
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
        return PlaceholderContainer.compileTrusted(placeholders).applyTrusted(messages).stream().map(MSCore::applyTransformers).collect(Collectors.toList());
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
        if (StringUtilCore.isEmpty(message))
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
}
