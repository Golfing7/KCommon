package com.golfing8.kcommon.util;

import com.golfing8.kcommon.util.StyledTextExtractor.StyledCharacter;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * Utility class for rendering styled text (including gradients) into Hytale UI.
 * Creates inline Label elements for each character with their specific colors.
 */
@SuppressWarnings("unused")
@UtilityClass
public class StyledTextRenderer {

    /**
     * Renders a list of styled characters into inline Label elements within a container.
     * Each character gets its own Label with the appropriate color.
     *
     * @param commands          The UICommandBuilder to append elements to
     * @param containerSelector The selector for the container element (e.g., "#TagNameContainer")
     * @param characters        The list of styled characters to render
     * @param fontSize          The font size for the labels
     * @param defaultBold       Whether to render bold by default (can be overridden by character style)
     */
    public static void render(
            UICommandBuilder commands,
            String containerSelector,
            List<StyledCharacter> characters,
            int fontSize,
            boolean defaultBold
    ) {
        for (int i = 0; i < characters.size(); i++) {
            StyledCharacter sc = characters.get(i);

            // Determine if this character should be bold
            boolean isBold = sc.bold() || defaultBold;
            boolean isItalic = sc.italic();

            // Build the inline label markup
            StringBuilder styleBuilder = new StringBuilder();
            styleBuilder.append("TextColor: ").append(sc.hexColor());
            styleBuilder.append(", FontSize: ").append(fontSize);

            if (isBold) {
                styleBuilder.append(", RenderBold: true");
            }
            if (isItalic) {
                styleBuilder.append(", RenderItalic: true");
            }

            String labelMarkup = String.format(
                    "Label { Style: (%s); Text: \"%s\"; }",
                    styleBuilder,
                    escapeText(sc.character())
            );

            commands.appendInline(containerSelector, labelMarkup);
        }
    }

    /**
     * Renders styled characters with default settings (fontSize 16, bold).
     *
     * @param commands          The UICommandBuilder to append elements to
     * @param containerSelector The selector for the container element
     * @param characters        The list of styled characters to render
     */
    public static void render(
            UICommandBuilder commands,
            String containerSelector,
            List<StyledCharacter> characters
    ) {
        render(commands, containerSelector, characters, 16, true);
    }

    /**
     * Escapes special characters for use in UI markup text.
     *
     * @param text The text to escape
     * @return The escaped text safe for use in UI markup
     */
    private static String escapeText(String text) {
        if (text == null) {
            return "";
        }
        // Escape quotes and backslashes for the markup string
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
