package com.golfing8.kcommon.versioned;

import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.util.Reflection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class VersionedTest {

    interface SampleVersioned extends Versioned {
    }

    @ServerVersionGroup(minimumMajorVersion = 8, maximumMajorVersion = 8)
    static class Version8Implementation implements SampleVersioned {
    }

    @ServerVersionGroup(minimumMajorVersion = 8, maximumMajorVersion = 8, minimumMinorVersion = 3, maximumMinorVersion = 5)
    static class Version8MinorRangeImplementation implements SampleVersioned {
    }

    static class UnannotatedImplementation implements SampleVersioned {
    }

    @ServerVersionGroup(minimumMajorVersion = 9, maximumMajorVersion = 9)
    static class Version9Implementation implements SampleVersioned {
    }

    @ServerVersionGroup(minimumMajorVersion = 10, maximumMajorVersion = 10)
    static final class NoNoArgsConstructorImplementation implements SampleVersioned {
        private NoNoArgsConstructorImplementation(String ignored) {
        }
    }

    @Nested
    @DisplayName("Implementation resolution")
    class ImplementationResolutionTests {

        @Test
        @DisplayName("Returns the first matching annotated implementation")
        void returnsMatchingImplementation() {
            try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class, CALLS_REAL_METHODS)) {
                reflection.when(() -> Reflection.getAllImplementors(SampleVersioned.class))
                        .thenReturn(new HashSet<>(Arrays.asList(UnannotatedImplementation.class, Version8Implementation.class)));

                Optional<SampleVersioned> result = Versioned.getImplementationFor(new NMSVersion(8, -1), SampleVersioned.class);

                assertTrue(result.isPresent());
                assertInstanceOf(Version8Implementation.class, result.get());
            }
        }

        @Test
        @DisplayName("Supports minor version ranges")
        void supportsMinorVersionRange() {
            try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class, CALLS_REAL_METHODS)) {
                reflection.when(() -> Reflection.getAllImplementors(SampleVersioned.class))
                        .thenReturn(Collections.singleton(Version8MinorRangeImplementation.class));

                Optional<SampleVersioned> result = Versioned.getImplementationFor(new NMSVersion(8, 4), SampleVersioned.class);

                assertTrue(result.isPresent());
                assertInstanceOf(Version8MinorRangeImplementation.class, result.get());
            }
        }

        @Test
        @DisplayName("Returns empty when no implementation matches")
        void returnsEmptyWhenNoMatch() {
            try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class, CALLS_REAL_METHODS)) {
                reflection.when(() -> Reflection.getAllImplementors(SampleVersioned.class))
                        .thenReturn(new HashSet<>(Arrays.asList(UnannotatedImplementation.class, Version9Implementation.class)));

                Optional<SampleVersioned> result = Versioned.getImplementationFor(new NMSVersion(8, -1), SampleVersioned.class);

                assertFalse(result.isPresent());
            }
        }

        @Test
        @DisplayName("Throws when a matching implementation has no no-args constructor")
        void throwsWhenNoNoArgsConstructorExists() {
            try (MockedStatic<Reflection> reflection = mockStatic(Reflection.class, CALLS_REAL_METHODS)) {
                reflection.when(() -> Reflection.getAllImplementors(SampleVersioned.class))
                        .thenReturn(Collections.singleton(NoNoArgsConstructorImplementation.class));

                IllegalStateException exception = assertThrows(IllegalStateException.class,
                        () -> Versioned.getImplementationFor(new NMSVersion(10, -1), SampleVersioned.class));

                assertEquals("Class " + NoNoArgsConstructorImplementation.class.getName() + " lacks no args constructor!", exception.getMessage());
            }
        }
    }
}
