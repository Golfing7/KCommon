package com.golfing8.kcommon.struct.reflection;

import com.golfing8.kcommon.util.Reflection;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a field's type and all generic types.
 */
@Getter
@AllArgsConstructor
public class FieldType {
    /**
     * Annotations on the field.
     */
    private final List<Annotation> annotations;
    /**
     * The field's type.
     */
    private final Class<?> type;
    /**
     * All generic types of the field
     */
    private final List<Type> genericTypes;

    public FieldType(Class<?> type) {
        this.annotations = Collections.emptyList();
        this.type = type;
        this.genericTypes = Collections.emptyList();
    }

    public FieldType(Field field) {
        this.annotations = Arrays.asList(field.getAnnotations());
        this.type = field.getType();
        this.genericTypes = Reflection.getParameterizedTypes(field);
    }

    public FieldType(Type type) {
        Preconditions.checkArgument(type instanceof Class<?> || type instanceof ParameterizedType);
        if (type instanceof AnnotatedElement) {
            this.annotations = Arrays.asList(((AnnotatedElement) type).getAnnotations());
        } else {
            this.annotations = Collections.emptyList();
        }
        if (type instanceof Class<?>) {
            this.type = (Class<?>) type;
            this.genericTypes = Collections.emptyList();
        } else {
            this.type = (Class<?>) ((ParameterizedType) type).getRawType();
            this.genericTypes = Arrays.asList(((ParameterizedType) type).getActualTypeArguments());
        }

    }

    /**
     * Extracts the field type from the given {@link TypeToken}
     *
     * @param token the token
     * @return the new field type
     */
    public static FieldType extractFrom(TypeToken<?> token) {
        ParameterizedType foundType = (ParameterizedType) token.getType();
        List<Annotation> annotations;
        if (token.getType() instanceof AnnotatedElement) {
            annotations = Arrays.asList(((AnnotatedElement) token.getType()).getAnnotations());
        } else {
            annotations = Collections.emptyList();
        }
        return new FieldType(annotations, (Class<?>) foundType.getRawType(),
                Lists.newArrayList(foundType.getActualTypeArguments()));
    }
}
