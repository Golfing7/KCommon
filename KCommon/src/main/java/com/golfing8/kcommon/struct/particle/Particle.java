package com.golfing8.kcommon.struct.particle;

import com.cryptomorin.xseries.particles.ParticleDisplay;
import com.cryptomorin.xseries.particles.XParticle;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.util.VectorUtil;
import lombok.Getter;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public abstract class Particle {
    public static final double ROOT_2 = Math.sqrt(2);
    public static final double ROOT_3 = Math.sqrt(3);

    //Pitch is X rotation
    //Yaw is Y rotation
    //Roll is Z rotation
    @Getter
    private double pitch, yaw, roll;

    public <T extends Particle> T pitch(double pitch) {
        this.pitch = pitch;
        return (T) this;
    }

    public <T extends Particle> T yaw(double yaw) {
        this.yaw = yaw;
        return (T) this;
    }

    public <T extends Particle> T roll(double roll) {
        this.roll = roll;
        return (T) this;
    }

    @Getter
    private double particleSize = 1.0D;

    public <T extends Particle> T particleSize(double size) {
        this.particleSize = size;
        return (T) this;
    }

    @Getter
    private Color from = Color.WHITE, to = Color.WHITE;

    //These generics let us do things like:
    //ParticleCircle circle = ...
    //circle = circle.from(Color.WHITE)
    //without recasting.
    public <T extends Particle> T from(Color from) {
        this.from = from;
        return (T) this;
    }

    public <T extends Particle> T to(Color to) {
        this.to = to;
        return (T) this;
    }

    /**
     * Sets both colors.
     *
     * @param color the color.
     * @param <T>   useful generic so working with method on subclasses is easier.
     * @return this, easier for building.
     */
    public <T extends Particle> T color(Color color) {
        return from(color).to(color);
    }

    public Particle() {
    }

    public Particle(double pitch, double yaw, double roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    protected Particle(ConfigurationSection section) {
        this.pitch = section.getDouble("pitch", 0.0);
        this.yaw = section.getDouble("yaw", 0.0);
        this.roll = section.getDouble("roll", 0.0);

        this.from = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString(section.getString("from-color", "FFFFFF")), Color.class);
        this.to = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofString(section.getString("to-color", "FFFFFF")), Color.class);

        this.particleSize = section.getDouble("particle-size", 1.0D);
    }

    public abstract ParticleType getParticleType();

    public Map<String, Object> toPrimitive() {
        Map<String, Object> primitive = new HashMap<>();
        primitive.put("pitch", pitch);
        primitive.put("yaw", yaw);
        primitive.put("roll", roll);
        primitive.put("from", from);
        primitive.put("to", to);
        primitive.put("particle-size", particleSize);
        return primitive;
    }

    /**
     * Meshes two colors into one with a certain percentage of the second color.
     *
     * @param first      the first color to mesh
     * @param second     the second color to mesh
     * @param percentage the decimal of the second color it will be. MUST BE {@code 0 <= percentage <= 1}! When
     *                   percentage is 1, the second color is returned, when it's 0, the first is returned.
     * @return the meshed color
     */
    protected Color meshColors(Color first, Color second, double percentage) {
        if (percentage < 0 || percentage > 1)
            throw new IllegalArgumentException("\"percentage\" value must be 0 <= percentage <= 1! Was given: " + percentage + "!");

        if (percentage == 0.0)
            return first;
        if (percentage == 1.0)
            return second;

        int meshedRed = (int) (first.getRed() + (second.getRed() - first.getRed()) * percentage);
        int meshedGreen = (int) (first.getGreen() + (second.getGreen() - first.getGreen()) * percentage);
        int meshedBlue = (int) (first.getBlue() + (second.getBlue() - first.getBlue()) * percentage);

        return new Color(meshedRed, meshedGreen, meshedBlue);
    }

    /**
     * Manipulates the provided vector to the angles set in this particle.
     *
     * @param vector the vector to manipulate.
     * @return the manipulated vector.
     */
    protected Vector manipulateToAngles(Vector vector) {
        VectorUtil.rotateAroundX(vector, Math.toRadians(getPitch()));
        VectorUtil.rotateAroundY(vector, Math.toRadians(getYaw()));
        VectorUtil.rotateAroundZ(vector, Math.toRadians(getRoll()));
        return vector;
    }

    /**
     * Spawns a colored dust particle at a location. Makes it neater for subclasses to spawn particles.
     *
     * @param location the location to spawn the particle at.
     */
    protected void spawnParticle(Location location) {
        if (NMS.getServerVersion().isAtOrBefore(NMSVersion.v1_8)) {
            location.getWorld().spigot().playEffect(location, Effect.COLOURED_DUST, 0, 0, Math.max(from.getRed() / 255.0F, 0.001F), from.getGreen() / 255.0F, from.getBlue() / 255.0F, 1, 0, 64);
        } else {
            ParticleDisplay.of(XParticle.DUST_COLOR_TRANSITION).withTransitionColor(from, (float) particleSize, to).spawn(location);
        }
    }

    /**
     * "Spawns" the particle at a given location.
     *
     * @param location the location to spawn the particle at
     */
    public abstract void spawnAt(Location location);
}
