package com.golfing8.kcommon.struct.placeholder;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A container for both {@link Placeholder} instances and {@link MultiLinePlaceholder} instances.
 */
@Getter
public class PlaceholderContainer {
    public static final PlaceholderContainer EMPTY = new PlaceholderContainer(Collections.emptyList(), Collections.emptyList());
    /**
     * Contains normal placeholders
     */
    private final ImmutableList<Placeholder> placeholders;
    /**
     * Contains multi-line placeholders
     */
    private final ImmutableList<MultiLinePlaceholder> multiLinePlaceholders;

    public PlaceholderContainer(List<Placeholder> placeholders, List<MultiLinePlaceholder> multiLinePlaceholders) {
        this.placeholders = ImmutableList.copyOf(placeholders);
        this.multiLinePlaceholders = ImmutableList.copyOf(multiLinePlaceholders);
    }

    private List<String> apply(List<String> input, boolean trusted) {
        List<String> toReturn = new ArrayList<>(input);

        // Loop over all the messages and parse them one at a time
        for (MultiLinePlaceholder placeholder : multiLinePlaceholders) {
            if (placeholder.isTrusted() != trusted)
                continue;

            toReturn = placeholder.apply(toReturn);
        }

        for (Placeholder placeholder : placeholders) {
            if (placeholder.isTrusted() != trusted)
                continue;

            for (int i = 0; i < toReturn.size(); i++) {
                toReturn.set(i, placeholder.apply(toReturn.get(i)));
            }
        }

        return toReturn;
    }

    /**
     * Applies the trusted placeholders to the given messages.
     *
     * @param input the input.
     * @return the messages.
     */
    public List<String> applyTrusted(List<String> input) {
        return apply(input, true);
    }

    /**
     * Applies the untrusted placeholders to the given messages.
     *
     * @param input the input.
     * @return the messages.
     */
    public List<String> applyUntrusted(List<String> input) {
        return apply(input, false);
    }

    /**
     * Applies the untrusted components to this placeholder.
     *
     * @param component the component.
     * @return the new component.
     */
    public Component applyUntrusted(final Component component) {
        Component toReturn = component;
        for (MultiLinePlaceholder placeholder : multiLinePlaceholders) {
            if (placeholder.isTrusted())
                continue;

            Component result = Component.empty();
            List<String> replacement = placeholder.getReplacement();
            for (int i = 0; i < replacement.size(); i++) {
                result = result.append(Component.text(replacement.get(i)));
                if (i + 1 != replacement.size())
                    result = result.appendNewline();
            }
            Component finalComponent = result;
            toReturn = toReturn.replaceText((builder) -> {
                builder.matchLiteral(placeholder.getLabel()).replacement(finalComponent);
            });
        }

        for (Placeholder placeholder : placeholders) {
            if (placeholder.isTrusted())
                continue;

            toReturn = toReturn.replaceText((builder) -> {
                builder.matchLiteral(placeholder.getLabel()).replacement(placeholder.getValue());
            });
        }
        return toReturn;
    }

    /**
     * Compiles a placeholder container from the given arguments.
     *
     * @param objects the objects.
     * @return the flattened placeholder container.
     */
    public static PlaceholderContainer compileTrusted(@NotNull Object @NotNull ... objects) {
        return compile(true, objects);
    }

    /**
     * Compiles a placeholder container from the given arguments.
     *
     * @param objects the objects.
     * @return the flattened placeholder container.
     */
    public static PlaceholderContainer compileUntrusted(@NotNull Object @NotNull ... objects) {
        return compile(false, objects);
    }

    private static PlaceholderContainer compile(boolean trusted, @NotNull Object @NotNull ... objects) {
        Preconditions.checkNotNull(objects, "Arguments cannot be null");
        if (objects.length == 0) {
            return EMPTY;
        }
        // Don't clone the container if there's no need.
        if (objects.length == 1 && objects[0] instanceof PlaceholderContainer) {
            return (PlaceholderContainer) objects[0];
        }

        List<Placeholder> placeholders = new ArrayList<>();
        List<MultiLinePlaceholder> multiLinePlaceholders = new ArrayList<>();
        compileAndFlattenInto(placeholders, multiLinePlaceholders, trusted, objects);
        return new PlaceholderContainer(placeholders, multiLinePlaceholders);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void compileAndFlattenInto(List<Placeholder> placeholders, List<MultiLinePlaceholder> multiLinePlaceholders, boolean trusted, Object[] objects) {
        for (int index = 0; index < objects.length; index++) {
            Object argument = objects[index];
            if (argument instanceof PlaceholderContainer) {
                placeholders.addAll(((PlaceholderContainer) argument).placeholders);
                multiLinePlaceholders.addAll(((PlaceholderContainer) argument).multiLinePlaceholders);
            } else if (argument instanceof Placeholder) {
                placeholders.add((Placeholder) argument);
            } else if (argument instanceof MultiLinePlaceholder) {
                multiLinePlaceholders.add((MultiLinePlaceholder) argument);
            } else if (argument instanceof Object[]) {
                compileAndFlattenInto(placeholders, multiLinePlaceholders, trusted, (Object[]) argument);
            } else if (argument instanceof Collection) {
                Collection collection = (Collection) argument;
                compileAndFlattenInto(placeholders, multiLinePlaceholders, trusted, collection.toArray());
            } else {
                if (index + 1 >= objects.length)
                    throw new IllegalArgumentException("Unbalanced placeholder list: " + Arrays.toString(objects));

                String key = Objects.toString(objects[index]).toUpperCase();
                Object value = objects[index + 1];
                if (value instanceof List) {
                    multiLinePlaceholders.add(MultiLinePlaceholder.percentTrustedArg(key, (List<Object>) value, trusted));
                } else {
                    placeholders.add(Placeholder.curlyTrustedArg(key, value, trusted));
                }
                index++;
            }
        }
    }
}
