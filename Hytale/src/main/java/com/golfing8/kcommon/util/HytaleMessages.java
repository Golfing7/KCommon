package com.golfing8.kcommon.util;

import com.hypixel.hytale.server.core.Message;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns KCommon's text (legacy '&' codes, hex colors and MiniMessage, parsed into Adventure components)
 * into Hytale {@link Message}s.
 */
public final class HytaleMessages {
    private HytaleMessages() {
    }

    /**
     * Parses the lines with KCommon's usual placeholders and formatting, then converts them.
     *
     * @param lines        the lines.
     * @param placeholders the placeholders.
     * @return the message.
     */
    public static Message toMessage(List<String> lines, Object... placeholders) {
        return toMessage(MSCore.toComponent(lines, placeholders));
    }

    /**
     * Parses the line with KCommon's usual placeholders and formatting, then converts it.
     *
     * @param line         the line.
     * @param placeholders the placeholders.
     * @return the message.
     */
    public static Message toMessage(String line, Object... placeholders) {
        return toMessage(MSCore.toComponent(line, placeholders));
    }

    /**
     * Converts an Adventure component to a Hytale message, keeping colors, bold, italic and links.
     *
     * @param component the component.
     * @return the message.
     */
    public static Message toMessage(Component component) {
        List<Message> parts = new ArrayList<>();
        append(component, Style.empty(), parts);
        return parts.isEmpty() ? Message.empty() : Message.join(parts.toArray(new Message[0]));
    }

    private static void append(Component component, Style parentStyle, List<Message> parts) {
        Style style = parentStyle.merge(component.style());
        if (component instanceof TextComponent) {
            String content = ((TextComponent) component).content();
            if (!content.isEmpty())
                parts.add(styled(Message.raw(content), style));
        }

        for (Component child : component.children()) {
            append(child, style, parts);
        }
    }

    private static Message styled(Message message, Style style) {
        TextColor color = style.color();
        if (color != null)
            message = message.color(color.asHexString());
        if (style.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE)
            message = message.bold(true);
        if (style.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE)
            message = message.italic(true);

        ClickEvent click = style.clickEvent();
        if (click != null && click.action() == ClickEvent.Action.OPEN_URL)
            message = message.link(((ClickEvent.Payload.Text) click.payload()).value());
        return message;
    }
}
