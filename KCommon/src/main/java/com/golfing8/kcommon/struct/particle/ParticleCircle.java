package com.golfing8.kcommon.struct.particle;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Spawns a "circle" of particles around a certain point with a radius.
 */
public class ParticleCircle extends Particle{
    @Getter
    protected double radius = 1.0;

    public ParticleCircle radius(double radius){
        this.radius = radius;
        return this;
    }

    @Override
    public void spawnAt(Location location) {
        int segments = (int) (radius * 24);
        for(int segment = 0; segment < segments; segment++)
        {
            Vector offset = new Vector(0, 0, 0);
            double angle = 360.0F * ((float) (segment + 1) / (float) segments);

            offset.setX(Math.cos(Math.toRadians(angle)) * radius);
            offset.setZ(Math.sin(Math.toRadians(angle)) * radius);

            rotateAroundX(offset, Math.toRadians(getPitch()));
            rotateAroundY(offset, Math.toRadians(getYaw()));
            rotateAroundZ(offset, Math.toRadians(getRoll()));

            Location particleLocation = location.clone().add(offset);

            spawnParticle(particleLocation);
        }
    }
}
