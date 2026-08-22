package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.DynamicEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional metadata for a {@link ConfigAdapter} implementation, describing the YAML shape it
 * produces. This has no effect on serialization at runtime - it exists purely so that the KCommon
 * IntelliJ plugin can discover custom adapters declared by projects that depend on KCommon, and
 * use them to validate/complete config fields without ever executing the adapter's code.
 * <p>
 * Only meaningful when placed on a class implementing {@link ConfigAdapter}.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigAdapterInfo {
    /**
     * The type this adapter adapts. Should match the adapter's {@code getAdaptType()}.
     *
     * @return the adapted type.
     */
    Class<?> value();

    /**
     * The YAML shape this adapter produces.
     *
     * @return the shape.
     */
    Shape shape() default Shape.UNKNOWN;

    /**
     * For {@link Shape#OBJECT}: the fixed set of mapping keys this type serializes to. Ignored
     * for other shapes.
     *
     * @return the known keys.
     */
    String[] keys() default {};

    /**
     * For {@link Shape#ENUM}: an explicit list of valid values. If left empty, the plugin falls
     * back to the adapted type's own enum constants (if it's a real enum), or the names of its
     * public static fields of its own type (for registry-style pseudo-enums, e.g. Bukkit's
     * PotionEffectType or a {@link DynamicEnum} subtype).
     *
     * @return the valid values, or empty to infer them from the adapted type.
     */
    String[] enumValues() default {};

    /**
     * The shape of YAML value a {@link ConfigAdapter} produces/consumes.
     */
    enum Shape {
        /**
         * No specific shape is declared - the plugin falls back to inferring structure from the
         * adapted type itself (e.g. still detecting it as CASerializable/enum/etc. if applicable).
         */
        UNKNOWN,
        /**
         * A fixed set of valid string values - see {@link #enumValues()}.
         */
        ENUM,
        /**
         * A YAML mapping with a fixed, known set of keys - see {@link #keys()}.
         */
        OBJECT,
    }
}
