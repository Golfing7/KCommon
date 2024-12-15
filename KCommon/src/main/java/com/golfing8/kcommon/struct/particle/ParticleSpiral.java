package com.golfing8.kcommon.struct.particle;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Map;

/**
 * Spawns a "spiral" of particles around a certain point with a radius.
 */
public class ParticleSpiral extends ParticleCircle{
    //The length of the spiral.
    @Getter
    protected double length = 1;
    public ParticleSpiral length(double length)
    {
        this.length = length;
        return this;
    }

    //The periods of the spiral.
    private double periods = 1;

    public ParticleSpiral periods(double periods)
    {
        this.periods = periods;
        return this;
    }

    public ParticleSpiral() {}

    protected ParticleSpiral(ConfigurationSection section) {
        super(section);

        this.length = section.getDouble("length", 1.0D);
        this.periods = section.getDouble("periods", 1.0D);
    }

    @Override
    public Map<String, Object> toPrimitive() {
        Map<String, Object> primitive = super.toPrimitive();
        primitive.put("length", this.length);
        primitive.put("periods", this.periods);
        return primitive;
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.SPIRAL;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        int segments = (int) (radius * 24);

        Vector dir = manipulateToAngles(new Vector(0, length / segments, 0).clone().divide(new Vector(periods, periods, periods)));

        double dAngle = 360.0F * (1.0D / segments);

        double finalAngle = 360.0F * periods;

        double angle = 0.0D;

        Location currentLocation = location.clone();
        while(angle <= finalAngle)
        {
            angle += dAngle;

            Vector offset = new Vector(0, 0.1, 0);

            offset.setX(Math.cos(Math.toRadians(angle)) * radius);
            offset.setZ(Math.sin(Math.toRadians(angle)) * radius);

            manipulateToAngles(offset);

            Location particleLocation = currentLocation.clone().add(offset);

            spawnParticle(players, particleLocation);

            currentLocation.add(dir);
        }
    }
}
