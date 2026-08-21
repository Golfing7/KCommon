package com.golfing8.kcommon.util;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReflectionTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface Marked {
    }

    static class Sample {
        @Marked
        private String markedField = "marked";
        private int unmarkedField = 42;

        public Sample() {
        }

        public Sample(int value) {
            this.unmarkedField = value;
        }

        private String secret() {
            return "secret-" + unmarkedField;
        }

        static class Nested1 {
            static class Nested2 {
            }
        }
    }

    static class GenericHolder {
        private List<String> stringList;
    }

    static class ParameterizedSuper extends ArrayList<String> {
    }

    static class WithNoArgsCtor {
        int value = 5;
    }

    static class NoZeroArgCtor {
        final int value;

        NoZeroArgCtor(int value) {
            this.value = value;
        }
    }

    @Nested
    @DisplayName("Generic type inspection")
    class GenericTypeInspection {
        @Test
        @DisplayName("getParameterizedTypes returns the field's generic type arguments")
        void testGetParameterizedTypes() throws NoSuchFieldException {
            Field field = GenericHolder.class.getDeclaredField("stringList");
            List<Type> types = Reflection.getParameterizedTypes(field);
            assertEquals(1, types.size());
            assertEquals(String.class, types.get(0));
        }

        @Test
        @DisplayName("getParameterizedTypes returns empty list for non-generic fields")
        void testGetParameterizedTypesNonGeneric() throws NoSuchFieldException {
            Field field = Sample.class.getDeclaredField("unmarkedField");
            assertTrue(Reflection.getParameterizedTypes(field).isEmpty());
        }

        @Test
        @DisplayName("getSuperParameterizedTypes returns the superclass's generic type arguments")
        void testGetSuperParameterizedTypes() {
            List<Class<?>> types = Reflection.getSuperParameterizedTypes(ParameterizedSuper.class);
            assertEquals(1, types.size());
            assertEquals(String.class, types.get(0));
        }

        @Test
        @DisplayName("getSuperParameterizedTypes returns empty list when the superclass isn't parameterized")
        void testGetSuperParameterizedTypesNone() {
            assertTrue(Reflection.getSuperParameterizedTypes(Sample.class).isEmpty());
        }
    }

    @Nested
    @DisplayName("Field discovery")
    class FieldDiscovery {
        @Test
        @DisplayName("getAllFields finds declared fields")
        void testGetAllFields() {
            Set<Field> fields = Reflection.getAllFields(Sample.class);
            Set<String> names = new java.util.HashSet<>();
            for (Field f : fields) names.add(f.getName());
            assertTrue(names.contains("markedField"));
            assertTrue(names.contains("unmarkedField"));
        }

        @Test
        @DisplayName("getAllFieldHandles maps field name to a usable handle")
        void testGetAllFieldHandles() {
            Map<String, FieldHandle<?>> handles = Reflection.getAllFieldHandles(Sample.class);
            assertTrue(handles.containsKey("markedField"));

            Sample sample = new Sample();
            assertEquals("marked", handles.get("markedField").get(sample));
        }

        @Test
        @DisplayName("getFieldsWithAnnotation only returns annotated fields")
        void testGetFieldsWithAnnotation() {
            Set<Field> fields = Reflection.getFieldsWithAnnotation(Sample.class, Marked.class);
            assertEquals(1, fields.size());
            assertEquals("markedField", fields.iterator().next().getName());
        }

        @Test
        @DisplayName("getAllFieldsUpToIncluding stops gathering once past the given parent")
        void testGetAllFieldsUpToIncluding() {
            Set<Field> fields = Reflection.getAllFieldsUpToIncluding(Sample.class, Sample.class);
            Set<String> names = new java.util.HashSet<>();
            for (Field f : fields) names.add(f.getName());
            assertTrue(names.contains("markedField"));
            assertTrue(names.contains("unmarkedField"));
        }
    }

    @Nested
    @DisplayName("Nested class discovery")
    class NestedClassDiscovery {
        @Test
        @DisplayName("getAllNestedClasses recursively finds nested classes")
        void testGetAllNestedClasses() {
            Set<Class<?>> nested = Reflection.getAllNestedClasses(Sample.class);
            assertTrue(nested.contains(Sample.Nested1.class));
            assertTrue(nested.contains(Sample.Nested1.Nested2.class));
        }
    }

    @Nested
    @DisplayName("MethodHandle lookups")
    class MethodHandleLookups {
        @Test
        @DisplayName("findGetter returns a working getter handle for a private field")
        void testFindGetter() throws Throwable {
            MethodHandle handle = Reflection.findGetter(Sample.class, "markedField");
            assertNotNull(handle);
            Sample sample = new Sample();
            assertEquals("marked", (String) handle.invoke(sample));
        }

        @Test
        @DisplayName("findGetter returns null for a nonexistent field")
        void testFindGetterMissing() {
            assertNull(Reflection.findGetter(Sample.class, "doesNotExist"));
        }

        @Test
        @DisplayName("findSetter returns a working setter handle for a private field")
        void testFindSetter() throws Throwable {
            MethodHandle handle = Reflection.findSetter(Sample.class, "unmarkedField");
            assertNotNull(handle);
            Sample sample = new Sample();
            handle.invoke(sample, 100);
            assertEquals(100, sample.unmarkedField);
        }

        @Test
        @DisplayName("findMethodHandle locates and invokes a private method")
        void testFindMethodHandle() throws Throwable {
            MethodHandle handle = Reflection.findMethodHandle(Sample.class, "secret");
            assertNotNull(handle);
            Sample sample = new Sample(7);
            assertEquals("secret-7", (String) handle.invoke(sample));
        }

        @Test
        @DisplayName("findMethodHandle returns null for a nonexistent method")
        void testFindMethodHandleMissing() {
            assertNull(Reflection.findMethodHandle(Sample.class, "doesNotExist"));
        }

        @Test
        @DisplayName("findConstructor locates and invokes a private/parameterized constructor")
        void testFindConstructor() throws Throwable {
            MethodHandle handle = Reflection.findConstructor(Sample.class, int.class);
            assertNotNull(handle);
            Sample sample = (Sample) handle.invoke(9);
            assertEquals(9, sample.unmarkedField);
        }

        @Test
        @DisplayName("findConstructor returns null for a nonexistent constructor signature")
        void testFindConstructorMissing() {
            assertNull(Reflection.findConstructor(Sample.class, String.class, String.class));
        }
    }

    @Nested
    @DisplayName("Class loading helpers")
    class ClassLoadingHelpers {
        @Test
        @DisplayName("forNameOptional finds an existing class")
        void testForNameOptionalFound() {
            Optional<Class<?>> found = Reflection.forNameOptional("java.lang.String");
            assertTrue(found.isPresent());
            assertEquals(String.class, found.get());
        }

        @Test
        @DisplayName("forNameOptional is empty for an unknown class name")
        void testForNameOptionalMissing() {
            assertFalse(Reflection.forNameOptional("com.not.a.Real.Class$$$").isPresent());
        }
    }

    @Nested
    @DisplayName("instantiateOrGet")
    class InstantiateOrGet {
        @Test
        @DisplayName("Uses the no-args constructor when available")
        void testUsesNoArgsConstructor() {
            WithNoArgsCtor instance = Reflection.instantiateOrGet(WithNoArgsCtor.class, () -> {
                throw new AssertionError("Supplier should not be called");
            });
            assertEquals(5, instance.value);
        }

        @Test
        @DisplayName("Falls back to the supplier when there's no no-args constructor")
        void testFallsBackToSupplier() {
            NoZeroArgCtor fallback = new NoZeroArgCtor(99);
            NoZeroArgCtor instance = Reflection.instantiateOrGet(NoZeroArgCtor.class, () -> fallback);
            assertSame(fallback, instance);
        }
    }

    @Nested
    @DisplayName("invokeQuietly")
    class InvokeQuietly {
        @Test
        @DisplayName("Invokes a method handle and returns its result")
        void testInvokeQuietly() {
            MethodHandle handle = Reflection.findMethodHandle(Sample.class, "secret");
            Sample sample = new Sample(3);
            String result = Reflection.invokeQuietly(handle, sample);
            assertEquals("secret-3", result);
        }
    }
}
