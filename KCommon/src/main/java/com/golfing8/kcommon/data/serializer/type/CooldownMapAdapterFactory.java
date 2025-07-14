package com.golfing8.kcommon.data.serializer.type;

import com.golfing8.kcommon.struct.map.CooldownMap;
import com.google.gson.*;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public enum CooldownMapAdapterFactory implements JsonSerializer<CooldownMap>, JsonDeserializer<CooldownMap> {
    INSTANCE;

    @Override
    public CooldownMap deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type keyType = parameterizedType.getActualTypeArguments()[0];

            Map backingMap = jsonDeserializationContext.deserialize(jsonElement, new CooldownMapType(new Type[]{keyType, Long.class}));
            return new CooldownMap(backingMap);
        } else {
            Map backingMap = jsonDeserializationContext.deserialize(jsonElement, new CooldownMapType(new Type[]{Object.class, Long.class}));
            return new CooldownMap(backingMap);
        }
    }

    @Override
    public JsonElement serialize(CooldownMap cooldownMap, Type type, JsonSerializationContext jsonSerializationContext) {
        if (cooldownMap == null)
            return JsonNull.INSTANCE;

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type keyType = parameterizedType.getActualTypeArguments()[0];

            return jsonSerializationContext.serialize(cooldownMap.getCooldowns(), new CooldownMapType(new Type[]{keyType, Long.class}));
        } else {
            return jsonSerializationContext.serialize(cooldownMap.getCooldowns(), new CooldownMapType(new Type[]{Object.class, Long.class}));
        }
    }

    @AllArgsConstructor
    private static class CooldownMapType implements ParameterizedType {
        private final @NotNull Type @NotNull [] types;

        @Override
        public @NotNull Type @NotNull [] getActualTypeArguments() {
            return types;
        }

        @Override
        public @NotNull Type getRawType() {
            return Map.class;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }
    }
}
