package com.golfing8.kcommon.versioned;

import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.util.Reflection;

import java.util.Optional;
import java.util.Set;

/**
 * An interface representing a base of versioned classes.
 * <p>
 * To use this class, create an interface (or abstract class) that implements this interface.
 * Then, create a group of implementations for that abstraction, each of which containing the {@link ServerVersionGroup} annotation.
 * When this is registered, KCommon will automatically select a compatible implementation with the current server version.
 * </p>
 */
public interface Versioned {

    /**
     * Gets the implementations for the given NMS version
     *
     * @param version the version
     * @param versionedClass the versioned class
     * @return the first implementation to match
     * @param <T> the type T
     */
    static <T extends Versioned> Optional<T> getImplementationFor(NMSVersion version, Class<T> versionedClass) {
        Set<Class<? extends T>> classes = Reflection.getAllImplementors(versionedClass);
        for (Class<? extends T> clazz : classes) {
            // Implementations lacking the server version group are not considered.
            if (!(clazz.isAnnotationPresent(ServerVersionGroup.class))) {
                continue;
            }

            ServerVersionGroup versionGroup = clazz.getAnnotation(ServerVersionGroup.class);
            if (version.isInRange(versionGroup.minimumMajorVersion(), versionGroup.maximumMajorVersion(), versionGroup.minimumMinorVersion(), versionGroup.maximumMinorVersion())) {
                return Optional.of(Reflection.instantiateOrGet(clazz, () -> {
                    throw new IllegalStateException("Class " + clazz.getName() + " lacks no args constructor!");
                }));
            }
        }
        return Optional.empty();
    }
}
