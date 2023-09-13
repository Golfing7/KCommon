package com.golfing8.kcommon.struct.reflection;

import com.golfing8.kcommon.util.Reflection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
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
}
