package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Interval;
import com.golfing8.kcommon.util.MathExpressions;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;

/**
 * Represents a particle "3d line" given by a multivariable function.
 * This could be a line, curve, etc.
 * <p>
 * The function works by providing the "start" location and offsetting the function "line" on the X, Z, and Y axis.
 * If other rotations are necessary, please use the pitch, yaw, and roll fields to manipulate the vector.
 */
public class ParticleXZFunction extends ParticleFunction {
    private final BiFunction<Double, Double, Double> function;
    private final Interval intervalX;
    private final Interval intervalZ;

    public ParticleXZFunction(BiFunction<Double, Double, Double> function, Interval intervalX, Interval intervalZ) {
        this.function = function;
        this.intervalX = intervalX;
        this.intervalZ = intervalZ;
    }

    protected ParticleXZFunction(ConfigurationSection section) {
        super(section);

        this.intervalX = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "intervalX"), Interval.class);
        this.intervalZ = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "intervalZ"), Interval.class);
        String biFunction = section.getString("function");
        this.function = (x, z) -> MathExpressions.evaluate(biFunction, "X", x, "Z", z);
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.XZ_FUNCTION;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        Iterator<Double> iterX = intervalX.iterator();
        Iterator<Double> iterZ = intervalZ.iterator();

        Location previousParticle = null;

        double previousFValue = 0.0D;

        while (iterX.hasNext() && iterZ.hasNext()) {
            double xMod = iterX.next();
            double zMod = iterZ.next();

            if (Double.isNaN(xMod) || Double.isNaN(zMod)) {
                previousParticle = null;
                continue;
            }

            double fValue = function.apply(xMod, zMod) * amplitude;

            if (Double.isNaN(fValue)) {
                previousParticle = null;
                continue;
            }

            Vector locationOffset = new Vector(xMod, fValue, zMod);

            manipulateToAngles(locationOffset);

            Location at = location.clone().add(locationOffset);

            if (!isLocationValid(at)) {
                previousParticle = null;
                continue;
            }

            if (isSmartFill() &&
                    previousParticle != null &&
                    previousParticle.distance(at) >= smartFillThreshold &&
                    Math.abs(fValue - previousFValue) < Math.max(smartFillMaxThreshold, smartFillMaxThreshold * amplitude)) {
                smartFillLocations(previousParticle, at);
            }

            spawnParticle(players, at);

            previousParticle = at;

            previousFValue = fValue;
        }
    }
}
