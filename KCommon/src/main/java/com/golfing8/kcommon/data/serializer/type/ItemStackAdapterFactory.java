package com.golfing8.kcommon.data.serializer.type;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

/**
 * A type adapter for bukkit {@link ItemStack} instances.
 */
public enum ItemStackAdapterFactory implements TypeAdapterFactory {
    INSTANCE;

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        if (!ItemStack.class.isAssignableFrom(typeToken.getRawType()))
            return null;

        return (TypeAdapter<T>) new TypeAdapterItemStack();
    }

    public static class TypeAdapterItemStack extends TypeAdapter<ItemStack> {

        @Override
        public void write(JsonWriter jsonWriter, ItemStack itemStack) throws IOException {

        }

        @Override
        public ItemStack read(JsonReader jsonReader) throws IOException {
            return null;
        }
    }
}
