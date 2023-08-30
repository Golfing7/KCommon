package com.golfing8.kcommon.util;

import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.google.common.collect.Lists;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

@UtilityClass
public final class Placeholders {
    public static String parseFully(String message, Object... placeholders){
        if (placeholders == null || placeholders.length == 0)
            return message;
        if (placeholders.length % 2 != 0)
            throw new IllegalArgumentException("Cannot parse a placeholder length that is not divisible by 2!");

        StringPlaceholders sp = new StringPlaceholders(message);

        List<String> keys = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (int i = 0; i < placeholders.length; i++) {
            Object placeholder = placeholders[i];

            if(i % 2 == 0) {
                if (placeholder instanceof Placeholder) {
                    Placeholder pObj = (Placeholder) placeholder;
                    keys.add(pObj.getLabel());
                    values.add(pObj.getValue());
                } else {
                    keys.add(placeholder.toString());
                }
            } else {
                values.add(placeholder);
            }
        }

        sp.keys(keys.toArray(new String[0]));
        sp.values(values.toArray(new Object[0]));
        return sp.get();
    }

    public static StringPlaceholders start(String message){
        return new StringPlaceholders(message);
    }

    public static class StringPlaceholders {
        final String message;
        final List<String> keys = Lists.newArrayList();
        final List<Supplier<Object>> values = Lists.newArrayList();

        StringPlaceholders(String message){
            this.message = message;
        }

        public StringPlaceholders keys(String... keys){
            this.keys.clear();
            Collections.addAll(this.keys, keys);
            return this;
        }
        public StringPlaceholders values(Object... values){
            this.values.clear();
            for(Object obj : values){
                this.values.add(() -> obj);
            }
            return this;
        }
        @SafeVarargs
        public final StringPlaceholders values(Supplier<Object>... values){
            this.values.clear();
            for(Object obj : values){
                this.values.add(() -> obj);
            }
            return this;
        }
        public String get(){
            String toSend = message;
            for (int i = 0; i < keys.size() && i < values.size(); i++) {
                toSend = toSend.replace("{" + keys.get(i) + "}", values.get(i).get().toString());
            }
            return toSend;
        }
        public StringPlaceholders send(Player player){
            String toSend = message;
            for (int i = 0; i < keys.size() && i < values.size(); i++) {
                toSend = toSend.replace("{" + keys.get(i) + "}", values.get(i).get().toString());
            }
            player.sendMessage(toSend);
            return this;
        }
    }
}
