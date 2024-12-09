package com.golfing8.kcommon.struct.particle;

import com.google.common.collect.Lists;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.List;

/**
 * Represents a sphere of X radius.
 */
public class ParticleSphere extends ParticleCircle{
    private final List<WaveEffect> effectList = Lists.newArrayList();
    private final int stepModifier = 24;

    public ParticleSphere() {}

    protected ParticleSphere(ConfigurationSection section) {
        super(section);
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.SPHERE;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        for(int index = 0; index < effectList.size(); index++)
        {
            WaveEffect effect = effectList.get(index);

            if(effect.tick++ == effect.maxTick)
            {
                effectList.remove(index--);
            }
        }

        double radius = this.radius;

        int steps = (int) (radius * (stepModifier / getParticleSize()));

        //The idea of the half steps allows us to iterate from -1 to 1.
        int halfSteps = steps / 2;

        //We make this the special divisor to ensure that our sphere is properly capped on both ends.
        //This ensure that our sin never reaches -1 or 1 exactly, just very close.
        //Which, in turn, keeps us from getting cos values of 0, breaking the top and bottom of our sphere.
        double stepDivisor = (steps / 2D) + 0.2D;

        for(int yStep = -halfSteps; yStep <= halfSteps; yStep++)
        {
            double ratioOffset = (double) yStep / stepDivisor;

            //We get the arcsin of the step. Starting at -pi/2. (Meaning X has an offset radius of 0)
            double arcSin = Math.asin(ratioOffset);

            //The cos (x offset) of the angle.
            double cos = Math.cos(arcSin);

            double xRadius = radius * cos;

            //Get our "sub circle's" iteration count.
            int subStep = (int) (xRadius * (stepModifier / getParticleSize()));

            for(int xStep = 0; xStep < subStep; xStep++)
            {
                //Get our angle offset.
                double angle = 360.0F * ((float) (xStep + 1) / (float) subStep);

                Vector offsetVector = new Vector();

                offsetVector.setY(ratioOffset * radius);

                //Set the x and z offsets.
                offsetVector.setX(Math.cos(Math.toRadians(angle)) * xRadius);
                offsetVector.setZ(Math.sin(Math.toRadians(angle)) * xRadius);

                //Manipulate it! (even though, technically speaking, spinning a sphere does nothing. It may allow us to add shaders to the colors of spheres later on.)
                manipulateToAngles(offsetVector);

                Location finalParticleLocation = location.clone().add(offsetVector);

                spawnParticle(players, finalParticleLocation);
            }
        }

        //We "mask" the top and bottom of the sphere with a single particle.
        Vector top = new Vector(0, radius, 0);

        manipulateToAngles(top);

        spawnParticle(players, location.clone().add(top));

        Vector bottom = new Vector(0, -radius, 0);

        manipulateToAngles(bottom);

        spawnParticle(players, location.clone().add(bottom));
    }

    public void startWaveEffectAt(Vector vector, Color waveColor, double angleWidth)
    {
        vector = vector.clone();

        //We normalize the vector to make sure we're on the dot for radius.
        vector.normalize();

        //Set the vector's endpoint to somewhere on the sphere.
        vector.multiply(radius);

        WaveEffect effect = new WaveEffect(waveColor, vector, angleWidth);

        this.effectList.add(effect);
    }

    public void startWaveEffectAt(Vector vector, Color waveColor, double angleWidth, double radiusSpeed)
    {
        vector = vector.clone();

        //We normalize the vector to make sure we're on the dot for radius.
        vector.normalize();

        //Set the vector's endpoint to somewhere on the sphere.
        vector.multiply(radius);

        WaveEffect effect = new WaveEffect(waveColor, vector, angleWidth);

        effect.maxTick = (int) (radiusSpeed * 24);

        this.effectList.add(effect);
    }

    /**
     * Wrapper class used for wave effects.
     */
     class WaveEffect {
        //The "tick" of the wave we're on.
        int tick;

        //By default, we take 10 ticks for every 1 radius the sphere has. Hopefully this makes the transition smooth enough.
        int maxTick = (int) (radius * 24);

        //This value is used to determine the maximum distance from the "center line" of the pulse we can be.
        final double angleWidth;

        final Color waveColor;

        final Vector startingPoint;

        WaveEffect(Color waveColor, Vector startingPoint, double angleWidth)
        {
            this.waveColor = waveColor;
            this.startingPoint = startingPoint;
            this.angleWidth = angleWidth;
        }

        /**
         * This gets the "perfect amount of distance" from our starting point.
         * @return the perfect distance from our starting point.
         */
        double getCurrentAngleOfRotation()
        {
            //The reason for - 90.0F is to make the SIN on a -1 to 1 angle.
            return ((double) tick / maxTick) * 180.0F;
        }

        /**
         * Gets the level of mesh to apply to the particle at its given distance.
         * @return the amount of mesh to apply to our color.
         */
        double getMeshLevel(Vector offset)
        {
            double dotProduct = offset.dot(startingPoint);

            double angle = Math.toDegrees(Math.acos(dotProduct / (startingPoint.length() * offset.length())));

            double currentBestAngle = getCurrentAngleOfRotation();

            double dAngle = Math.abs(currentBestAngle - angle);

            double angleRadius = angleWidth / 2;

            //If we're outside, entirely, of the width, return 0!
            if(dAngle > angleRadius)
                return 0.0D;


            //This gets the ratio, from 0 to 1 of the two numbers.
            //Since it approaches 0 as it gets closer, we have to flip it around to get it to actually fit the necessary bill.
            return (dAngle / angleRadius - 1) * -1;
        }
    }
}
