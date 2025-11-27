package com.golfing8.kcommon.struct.placeholder;

import com.golfing8.kcommon.util.MS;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

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

    private List<Component> applyComponents(final List<Component> components, boolean trusted) {
        List<Component> toReturn = new ArrayList<>(components);
        for (MultiLinePlaceholder placeholder : multiLinePlaceholders) {
            if (placeholder.isTrusted() != trusted)
                continue;

            List<Component> componentsToAdd = new ArrayList<>();
            if (placeholder.isTrusted()) {
                componentsToAdd.addAll(MS.toComponentList(placeholder.getReplacement()));
            } else {
                componentsToAdd.addAll(placeholder.getReplacement().stream().map(Component::text).collect(Collectors.toList()));
            }

            // Loop over all components and parse them.
            for (int i = 0; i < toReturn.size(); i++) {
                Component parseable = toReturn.get(i);

                // We must encode, split, re-encode.
                String encodedComponent = MiniMessage.miniMessage().serialize(parseable);
                String[] splitEncode = encodedComponent.split(placeholder.getLabel());

                // If the label is not present, do nothing.
                if (splitEncode.length == 1)
                    continue;

                // Otherwise, add the components
                if (componentsToAdd.isEmpty()) {
                    toReturn.remove(i--);
                    continue;
                }

                toReturn.set(i, componentsToAdd.get(0));
                for (int j = 1; j < splitEncode.length; j++) {
                    toReturn.add(i + j, componentsToAdd.get(j));
                }
            }
        }

        for (Placeholder placeholder : placeholders) {
            if (placeholder.isTrusted() != trusted)
                continue;

            toReturn = toReturn.stream().map(parseable -> {
                return parseable.replaceText(builder -> {
                    if (placeholder.isTrusted()) {
                        builder.matchLiteral(placeholder.getLabel()).replacement(MS.toComponent(placeholder.getValue()));
                    } else {
                        builder.matchLiteral(placeholder.getLabel()).replacement(placeholder.getValue());
                    }
                });
            }).collect(Collectors.toList());
        }
        return toReturn;
    }

    /**
     * Applies the untrusted components to a single component placeholder.
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
     * Applies the trusted placeholders to the given list of components
     *
     * @param input the input
     * @return the parsed components
     */
    public List<Component> applyComponentsTrusted(List<Component> input) {
        return applyComponents(input, true);
    }

    /**
     * Applies the untrusted placeholders to the given list of components
     *
     * @param input the input
     * @return the parsed components
     */
    public List<Component> applyComponentsUntrusted(List<Component> input) {
        return applyComponents(input, false);
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
