package com.golfing8.kcommon.util;

import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@UtilityClass
public final class Placeholders {
    public static String parseFully(String message, Object... placeholders) {
        if (placeholders == null || placeholders.length == 0)
            return message;

        StringPlaceholders sp = new StringPlaceholders(message);

        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        boolean awaitingValue = false;
        for (Object placeholder : placeholders) {
            if (awaitingValue) {
                values.add(Objects.toString(placeholder));
                awaitingValue = false;
            } else {
                if (placeholder instanceof Placeholder) {
                    Placeholder pObj = (Placeholder) placeholder;
                    keys.add(pObj.getLabel()); // Assume the keys from the placeholder are already formatted properly.
                    values.add(pObj.getValue());
                    continue;
                }

                keys.add("{" + placeholder + "}");
                awaitingValue = true;
            }
        }
        if (awaitingValue) {
            throw new IllegalArgumentException(String.format("Unbalanced placeholders provided! keys=%s values=%s", keys, values));
        }

        sp.keys(keys.toArray(new String[0]));
        sp.values(values.toArray(new Object[0]));
        return sp.get();
    }

    public static StringPlaceholders start(String message) {
        return new StringPlaceholders(message);
    }

    public static class StringPlaceholders {
        final String message;
        final List<String> keys = Lists.newArrayList();
        final List<Supplier<Object>> values = Lists.newArrayList();

        StringPlaceholders(String message) {
            this.message = message;
        }

        public StringPlaceholders keys(String... keys) {
            this.keys.clear();
            Collections.addAll(this.keys, keys);
            return this;
        }

        public StringPlaceholders values(Object... values) {
            this.values.clear();
            for (Object obj : values) {
                this.values.add(() -> obj);
            }
            return this;
        }

        @SafeVarargs
        public final StringPlaceholders values(Supplier<Object>... values) {
            this.values.clear();
            for (Object obj : values) {
                this.values.add(() -> obj);
            }
            return this;
        }

        public String get() {
            String toSend = message;
            for (int i = 0; i < keys.size() && i < values.size(); i++) {
                Object o = values.get(i).get();
                String value = o == null ? "null" : o.toString();
                toSend = toSend.replace(keys.get(i), value);
            }
            return toSend;
        }

        public StringPlaceholders send(Player player) {
            player.sendMessage(get());
            return this;
        }
    }
}
