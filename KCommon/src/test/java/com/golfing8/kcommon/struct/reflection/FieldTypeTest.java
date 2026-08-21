package com.golfing8.kcommon.struct.reflection;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldTypeTest {

    static class Holder {
        List<String> stringList;
        Map<String, Integer> stringToIntMap;
        int plainInt;
    }

    @Test
    void testSimpleClassConstructor() {
        FieldType fieldType = new FieldType(String.class);
        assertEquals(String.class, fieldType.getType());
        assertTrue(fieldType.getAnnotations().isEmpty());
        assertTrue(fieldType.getGenericTypes().isEmpty());
    }

    @Test
    void testFromFieldExtractsRawTypeAndGenerics() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("stringList");
        FieldType fieldType = new FieldType(field);

        assertEquals(List.class, fieldType.getType());
        assertEquals(1, fieldType.getGenericTypes().size());
        assertEquals(String.class, fieldType.getGenericTypes().get(0));
    }

    @Test
    void testFromFieldWithMultipleGenerics() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("stringToIntMap");
        FieldType fieldType = new FieldType(field);

        assertEquals(Map.class, fieldType.getType());
        assertEquals(2, fieldType.getGenericTypes().size());
        assertEquals(String.class, fieldType.getGenericTypes().get(0));
        assertEquals(Integer.class, fieldType.getGenericTypes().get(1));
    }

    @Test
    void testFromPlainIntField() throws NoSuchFieldException {
        Field field = Holder.class.getDeclaredField("plainInt");
        FieldType fieldType = new FieldType(field);

        assertEquals(int.class, fieldType.getType());
        assertTrue(fieldType.getGenericTypes().isEmpty());
    }

    @Test
    void testExtractFromTypeToken() {
        TypeToken<List<String>> token = new TypeToken<List<String>>() {
        };
        FieldType fieldType = FieldType.extractFrom(token);

        assertEquals(List.class, fieldType.getType());
        assertEquals(1, fieldType.getGenericTypes().size());
        assertEquals(String.class, fieldType.getGenericTypes().get(0));
    }
}
