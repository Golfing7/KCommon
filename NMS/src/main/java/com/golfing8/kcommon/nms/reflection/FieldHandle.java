package com.golfing8.kcommon.nms.reflection;

import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class FieldHandle<T> {
    private final String fieldName;
    private final Class<?> clazz;
    @Getter
    private final Field field;

    public FieldHandle(Field field) {
        this.fieldName = field.getName();
        this.field = field;
        this.field.setAccessible(true);
        this.clazz = field.getDeclaringClass();
    }

    public FieldHandle(String fieldName, Class<T> clazz) {
        this.fieldName = fieldName;
        this.clazz = clazz;
        try {
            this.field = clazz.getDeclaredField(fieldName);

            this.field.setAccessible(true);
        } catch (NoSuchFieldException exc) {
            throw new RuntimeException(exc);
        }
    }

    public boolean shouldSerialize() {
        int modifiers = field.getModifiers();
        return (modifiers & Modifier.TRANSIENT) == 0 && (modifiers & Modifier.STATIC) == 0;
    }

    public void set(Object object, Object value) {
        // Cannot set primitive fields to null.
        if (field.getType().isPrimitive() && value == null)
            return;

        try {
            field.set(object, value);
            return;
        } catch (IllegalAccessException ignored) {
            //Shouldn't happen.
        }
        throw new RuntimeException(String.format("Couldn't set value of field %s!", field.getName()));
    }

    @SuppressWarnings("unchecked")
    public T get(Object object) {
        try {
            return (T) field.get(object);
        } catch (IllegalAccessException ignored) {
            //Shouldn't happen.
        }
        throw new RuntimeException(String.format("Couldn't retrieve value of field %s!", field.getName()));
    }
}
