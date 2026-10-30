package com.golfing8.kcommon.util;

import com.golfing8.kcommon.ComponentUtils;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for extracting per-character styling information from MiniMessage-formatted text.
 * This is useful for rendering gradient text in UIs that only support single-color labels.
 */
@UtilityClass
public class StyledTextExtractor {

    private static final String DEFAULT_COLOR = "#ffffff";

    /**
     * Extracts styled characters from a MiniMessage-formatted string.
     * Parses the string using MiniMessage and iterates through the component tree
     * to extract each character with its interpolated color (useful for gradients).
     *
     * @param miniMessageFormat The MiniMessage-formatted string (e.g., "&lt;gradient:gold:yellow&gt;VIP&lt;/gradient&gt;")
     * @return A list of StyledCharacter records containing each character and its hex color
     */
    public static List<StyledCharacter> extract(String miniMessageFormat) {
        if (miniMessageFormat == null || miniMessageFormat.isEmpty()) {
            return List.of();
        }

        Component component = ComponentUtils.toComponent(miniMessageFormat);
        List<StyledCharacter> result = new ArrayList<>();

        extractFromComponent(component, null, result);

        return result;
    }

    /**
     * Recursively extracts styled characters from a component and its children.
     *
     * @param component      The component to extract from
     * @param inheritedStyle The style inherited from parent components (null if none)
     * @param result         The list to add extracted characters to
     */
    private static void extractFromComponent(Component component, Style inheritedStyle, List<StyledCharacter> result) {
        // Merge styles: component's own style takes precedence over inherited
        Style currentStyle = mergeStyles(inheritedStyle, component.style());

        // Extract text content if this is a TextComponent
        if (component instanceof TextComponent textComponent) {
            String content = textComponent.content();
            if (!content.isEmpty()) {
                // Get color from current style
                TextColor color = currentStyle.color();
                String hexColor = color != null ? color.asHexString() : DEFAULT_COLOR;

                // Get decorations
                boolean bold = currentStyle.decoration(TextDecoration.BOLD) == TextDecoration.State.TRUE;
                boolean italic = currentStyle.decoration(TextDecoration.ITALIC) == TextDecoration.State.TRUE;

                // Add each character with its style
                for (int i = 0; i < content.length(); i++) {
                    String character = String.valueOf(content.charAt(i));
                    result.add(new StyledCharacter(character, hexColor, bold, italic));
                }
            }
        }

        // Recursively process children with the current style as inherited
        for (Component child : component.children()) {
            extractFromComponent(child, currentStyle, result);
        }
    }

    /**
     * Merges two styles, with the child style taking precedence for any explicitly set values.
     *
     * @param parent The parent/inherited style (can be null)
     * @param child  The child/current style
     * @return The merged style
     */
    private static Style mergeStyles(Style parent, Style child) {
        if (parent == null) {
            return child;
        }
        if (child == null) {
            return parent;
        }

        // Start with parent and merge child on top
        Style.Builder builder = parent.toBuilder();

        // Color: child takes precedence if set
        if (child.color() != null) {
            builder.color(child.color());
        }

        // Decorations: child takes precedence if explicitly set (not NOT_SET)
        for (TextDecoration decoration : TextDecoration.values()) {
            TextDecoration.State childState = child.decoration(decoration);
            if (childState != TextDecoration.State.NOT_SET) {
                builder.decoration(decoration, childState);
            }
        }

        return builder.build();
    }

    /**
     * Represents a single character with its associated style information.
     *
     * @param character The character as a string
     * @param hexColor  The hex color code (e.g., "#ff0000")
     * @param bold      Whether the character is bold
     * @param italic    Whether the character is italic
     */
    public record StyledCharacter(String character, String hexColor, boolean bold, boolean italic) {
    }
}
