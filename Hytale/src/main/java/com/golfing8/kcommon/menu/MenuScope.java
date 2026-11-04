package com.golfing8.kcommon.menu;

import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The bindings, placeholders and overrides of a set of elements: the whole menu for a {@link MenuBuilder},
 * or one copy of a template for a {@link MenuRow}. Elements are addressed by their config key.
 *
 * @param <S> the scope type, so calls chain.
 */
public abstract class MenuScope<S extends MenuScope<S>> {
    final Map<String, Binding> bindings = new LinkedHashMap<>();
    final Set<String> shown = new HashSet<>();
    final List<Object> placeholders = new ArrayList<>();
    final Map<String, List<Object>> elementPlaceholders = new HashMap<>();
    final Map<String, Map<String, Object>> overrides = new HashMap<>();
    final Map<String, String> texts = new HashMap<>();

    /**
     * An action bound to an element, and the event that fires it.
     */
    static final class Binding {
        final CustomUIEventBindingType type;
        final ClickAction action;

        Binding(CustomUIEventBindingType type, ClickAction action) {
            this.type = type;
            this.action = action;
        }
    }

    abstract @Nullable MenuElement element(String key);

    abstract S self();

    /**
     * Binds the action to the element, run when the player activates it.
     * A {@code special} element only appears once it is bound or {@link #show(String) shown}.
     *
     * @param key    the element's key.
     * @param action the action.
     * @return this scope.
     */
    public S bindTo(String key, ClickAction action) {
        return bindTo(key, CustomUIEventBindingType.Activating, action);
    }

    /**
     * Binds the action to an event of the element, such as {@link CustomUIEventBindingType#ValueChanged}.
     *
     * @param key    the element's key.
     * @param type   the event.
     * @param action the action.
     * @return this scope.
     */
    public S bindTo(String key, CustomUIEventBindingType type, ClickAction action) {
        require(key);
        bindings.put(key, new Binding(type, action));
        shown.add(key);
        return self();
    }

    /**
     * Shows a {@code special} element without binding anything to it.
     *
     * @param key the element's key.
     * @return this scope.
     */
    public S show(String key) {
        require(key);
        shown.add(key);
        return self();
    }

    /**
     * Adds placeholders for every element in this scope. Takes {@code Placeholder}s, collections of them,
     * or label and value pairs like {@code "COINS", 100}; pairs are trusted, so pass
     * {@code Placeholder.curlyUntrusted} for anything a player wrote.
     *
     * @param placeholders the placeholders.
     * @return this scope.
     */
    public S placeholders(Object... placeholders) {
        this.placeholders.addAll(Arrays.asList(placeholders));
        return self();
    }

    /**
     * Adds placeholders for the element and its children, the same way {@link #placeholders(Object...)} does.
     *
     * @param key          the element's key.
     * @param placeholders the placeholders.
     * @return this scope.
     */
    public S specialPlaceholders(String key, Object... placeholders) {
        require(key);
        elementPlaceholders.computeIfAbsent(key, k -> new ArrayList<>()).addAll(Arrays.asList(placeholders));
        return self();
    }

    /**
     * Sets a property of the element, replacing what the config says, such as {@code set("icon", "item-id", id)}.
     *
     * @param key      the element's key.
     * @param property the property, as in the config ({@code item-id}) or in markup ({@code ItemId}).
     * @param value    the value.
     * @return this scope.
     */
    public S set(String key, String property, Object value) {
        MenuElement element = require(key);
        String name = MenuSchema.pascal(property);
        MenuSchema.Property definition = element.property(name);
        if (definition == null)
            throw new IllegalArgumentException(element.path + " is a " + element.type + ", which has no property " + name);
        overrides.computeIfAbsent(key, k -> new LinkedHashMap<>()).put(name, value);
        return self();
    }

    /**
     * Shows or hides the element.
     *
     * @param key     the element's key.
     * @param visible if it should be visible.
     * @return this scope.
     */
    public S visible(String key, boolean visible) {
        return set(key, "Visible", visible);
    }

    /**
     * Enables or disables the button.
     *
     * @param key      the element's key.
     * @param disabled if it should be disabled.
     * @return this scope.
     */
    public S disabled(String key, boolean disabled) {
        return set(key, "Disabled", disabled);
    }

    /**
     * Replaces the element's text. It is formatted like the config's text, placeholders included.
     *
     * @param key  the element's key.
     * @param text the text.
     * @return this scope.
     */
    public S text(String key, String text) {
        MenuElement element = require(key);
        if (!MenuSchema.TEXT_TYPES.contains(element.type))
            throw new IllegalArgumentException(element.path + " is a " + element.type + ", which has no text");
        texts.put(key, text);
        return self();
    }

    MenuElement require(String key) {
        MenuElement element = element(key);
        if (element == null)
            throw new IllegalArgumentException("No element '" + key + "' in this menu");
        return element;
    }
}
