package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.particle.Particle;
import com.golfing8.kcommon.struct.particle.ParticleType;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CAParticle implements ConfigAdapter<Particle> {
    @Override
    public Class<Particle> getAdaptType() {
        return Particle.class;
    }

    @Override
    public @Nullable Particle toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> data = entry.unwrap();
        if (!data.containsKey("type")) {
            throw new InvalidConfigException("Type key missing for Particle adapter");
        }
        ParticleType particleType = ParticleType.valueOf(data.get("type").toString().toUpperCase());
        return particleType.fromConfig(entry.getSource());
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Particle object) {
        Map<String, Object> data = object.toPrimitive();
        data.put("type", object.getParticleType().name());
        data.putAll(object.toPrimitive());
        return ConfigPrimitive.ofMap(data);
    }
}
