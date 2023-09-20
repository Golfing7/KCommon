package com.golfing8.kcommon.struct.reflection;

import com.golfing8.kcommon.util.Reflection;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a field's type and all generic types.
 */
@Getter
@AllArgsConstructor
public class FieldType {
    /** The field's type. */
    private final Class<?> type;
    /** All generic types of the field */
    private final List<Class<?>> genericTypes;

    public FieldType(Class<?> type) {
        this.type = type;
        this.genericTypes = new ArrayList<>();
    }

    public FieldType(Field field) {
        this.type = field.getType();
        this.genericTypes = Reflection.getParameterizedTypes(field);
    }

    public static FieldType extractFrom(TypeToken<?> token) {
        ParameterizedType foundType = (ParameterizedType) token.getType();
        return new FieldType((Class<?>) foundType.getRawType(),
                Lists.newArrayList((Class<?>) foundType.getActualTypeArguments()[0]));
    }
}
