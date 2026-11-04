package com.golfing8.kcommon.menu;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.ui.Value;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * One built menu: its UI document, the commands that follow it and the actions its elements run.
 */
public final class Menu {
    /** The inline UI document. */
    @Getter
    private final String markup;
    final List<Object[]> sets;
    final List<Object[]> events;
    private final Map<String, ClickAction> actions;
    @Getter
    private final CustomPageLifetime lifetime;

    Menu(String markup, List<Object[]> sets, List<Object[]> events, Map<String, ClickAction> actions, CustomPageLifetime lifetime) {
        this.markup = markup;
        this.sets = Collections.unmodifiableList(sets);
        this.events = Collections.unmodifiableList(events);
        this.actions = Collections.unmodifiableMap(actions);
        this.lifetime = lifetime;
    }

    /**
     * Writes the menu into a page build.
     *
     * @param commands the page's commands.
     * @param events   the page's event bindings.
     */
    @SuppressWarnings("unchecked")
    void apply(UICommandBuilder commands, UIEventBuilder events) {
        commands.appendInline(null, markup);
        for (Object[] set : sets) {
            String selector = (String) set[0];
            Object value = set[1];
            if (value instanceof Value) {
                commands.set(selector, (Value<Object>) value);
            } else if (value instanceof Message) {
                commands.set(selector, (Message) value);
            } else {
                commands.set(selector, String.valueOf(value));
            }
        }
        for (Object[] event : this.events) {
            events.addEventBinding((CustomUIEventBindingType) event[0], (String) event[1], (EventData) event[2], false);
        }
    }

    /**
     * Gets the action a binding id the client sent refers to.
     *
     * @param id the id, straight from the client.
     * @return the action, or null if this build has no such binding.
     */
    @Nullable ClickAction action(@Nullable String id) {
        return id == null ? null : actions.get(id);
    }
}
