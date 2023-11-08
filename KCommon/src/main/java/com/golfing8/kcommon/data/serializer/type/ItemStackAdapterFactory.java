package com.golfing8.kcommon.data.serializer.type;

import com.golfing8.kcommon.util.ItemBase64;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A type adapter for bukkit {@link ItemStack} instances.
 */
//TODO This is only registered for ItemStack so it's currently OK... Fix it after a while!
public enum ItemStackAdapterFactory implements JsonSerializer<ConfigurationSerializable>, JsonDeserializer<ConfigurationSerializable> {
    INSTANCE;

    final Type objectStringMapType = new TypeToken<Map<String, Object>>() {}.getType();

    @Override
    public ConfigurationSerializable deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        // Need to serialize the old way, in this case.
        if (jsonElement.isJsonObject()) {
            final Map<String, Object> map = new LinkedHashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
                final JsonElement value = entry.getValue();
                final String name = entry.getKey();

                if (value.isJsonObject() && value.getAsJsonObject().has(ConfigurationSerialization.SERIALIZED_TYPE_KEY)) {
                    map.put(name, this.deserialize(value, value.getClass(), context));
                } else {
                    map.put(name, context.deserialize(value, Object.class));
                }
            }

            return ConfigurationSerialization.deserializeObject(map);
        } else {
            try {
                return ItemBase64.itemStackFromBase64(jsonElement.getAsString());
            } catch (IOException e) {
                // Nothing to return here...
                return null;
            }
        }
    }

    @Override
    public JsonElement serialize(ConfigurationSerializable src, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(ItemBase64.toBase64((ItemStack) src));
    }
}
