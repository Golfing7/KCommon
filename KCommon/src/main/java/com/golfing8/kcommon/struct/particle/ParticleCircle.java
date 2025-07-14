package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.util.VectorUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Spawns a "circle" of particles around a certain point with a radius.
 */
public class ParticleCircle extends Particle {
    @Getter
    protected double radius = 1.0;

    public ParticleCircle radius(double radius) {
        this.radius = radius;
        return this;
    }

    public ParticleCircle() {
    }

    protected ParticleCircle(ConfigurationSection section) {
        super(section);
        this.radius = section.getDouble("radius", 1.0D);
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.CIRCLE;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        int segments = (int) (radius * 24);
        for (int segment = 0; segment < segments; segment++) {
            Vector offset = new Vector(0, 0, 0);
            double angle = 360.0F * ((float) (segment + 1) / (float) segments);

            offset.setX(Math.cos(Math.toRadians(angle)) * radius);
            offset.setZ(Math.sin(Math.toRadians(angle)) * radius);

            VectorUtil.rotateAroundX(offset, Math.toRadians(getPitch()));
            VectorUtil.rotateAroundY(offset, Math.toRadians(getYaw()));
            VectorUtil.rotateAroundZ(offset, Math.toRadians(getRoll()));

            Location particleLocation = location.clone().add(offset);

            spawnParticle(players, particleLocation);
        }
    }
}
