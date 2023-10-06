package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.struct.Interval;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.function.Function;

/**
 * Represents a particle "line" given by a function.
 * This could be a line, curve, etc.
 *
 * The function works by providing the "start" location and offsetting the function "line" on the X and Y axis.
 * If other rotations are necessary, please use the pitch, yaw, and roll fields to manipulate the vector.
 */
public class ParticleXFunction extends ParticleFunction{
    private final Function<Double, Double> function;
    private final Interval interval;

    public ParticleXFunction(Function<Double, Double> function, Interval interval)
    {
        this.function = function;
        this.interval = interval;
    }

    @Override
    public void spawnAt(Location location) {
        Location previousParticle = null;

        double previousFValue = 0.0D;

        for(double offset : interval)
        {
            double fValue = function.apply(offset) * amplitude;

            if(Double.isNaN(fValue))
            {
                previousParticle = null;
                continue;
            }

            Vector locationOffset = new Vector(offset, fValue, 0);

            manipulateToAngles(locationOffset);

            Location at = location.clone().add(locationOffset);

            if(!isLocationValid(at))
            {
                previousParticle = null;
                continue;
            }

            if(isSmartFill() &&
                    previousParticle != null &&
                    previousParticle.distance(at) >= smartFillThreshold &&
                    (Math.abs(fValue - previousFValue) < Math.max(smartFillMaxThreshold, smartFillMaxThreshold * amplitude)))
            {
                smartFillLocations(previousParticle, at);
            }

            spawnParticle(at);

            previousParticle = at;

            previousFValue = fValue;
        }
    }
}
