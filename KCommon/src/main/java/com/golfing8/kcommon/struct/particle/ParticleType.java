package com.golfing8.kcommon.struct.particle;

import lombok.AllArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.function.Function;

/**
 * Acts as a registry for all the particle types.
 */
@AllArgsConstructor
public enum ParticleType {
    CIRCLE(ParticleCircle::new),
    COMPOUND(ParticleCompound::new),
    X_FUNCTION(ParticleXFunction::new),
    XZ_FUNCTION(ParticleXZFunction::new),
    LINE(ParticleLine::new),
    SPHERE(ParticleSphere::new),
    SPIRAL(ParticleSpiral::new),
    ;

    final Function<ConfigurationSection, Particle> constructor;

    /**
     * Loads the given particle instance from a config section
     *
     * @param section the section
     * @return this
     */
    public Particle fromConfig(ConfigurationSection section) {
        return constructor.apply(section);
    }
}
