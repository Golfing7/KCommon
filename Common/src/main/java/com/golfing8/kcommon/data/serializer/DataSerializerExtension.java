package com.golfing8.kcommon.data.serializer;

import com.google.gson.GsonBuilder;

/**
 * Adds platform specific types to {@link DataSerializer}.
 * <p>
 * Implementations are found with {@link java.util.ServiceLoader} when {@link DataSerializer} first loads.
 * </p>
 */
public interface DataSerializerExtension {
    /**
     * Registers type adapters on the builder used for KCommon's Gson instance.
     *
     * @param builder the builder.
     */
    void configure(GsonBuilder builder);

    /**
     * Registers transformers with {@link DataSerializer#registerTransformer}.
     */
    default void registerTransformers() {
    }
}
