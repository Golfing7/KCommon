package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.Pair;
import com.google.common.collect.Lists;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.*;

/**
 * Represents a collection of {@link Particle} that display around some central point
 * <p>
 * This class allows for even more complex displays
 * </p>
 */
public class ParticleCompound extends Particle {
    private final List<Pair<Particle, Vector>> particleOffset;

    @Override
    public <T extends Particle> T pitch(double pitch) {
        double dPitch = pitch - this.getPitch();

        particleOffset.forEach(part -> part.getA().pitch(part.getA().getPitch() + dPitch));

        return super.pitch(pitch);
    }

    @Override
    public <T extends Particle> T yaw(double yaw) {
        double dYaw = yaw - this.getYaw();

        particleOffset.forEach(part -> part.getA().yaw(part.getA().getYaw() + dYaw));

        return super.yaw(yaw);
    }

    @Override
    public <T extends Particle> T roll(double roll) {
        double dRoll = roll - this.getRoll();

        particleOffset.forEach(part -> part.getA().roll(part.getA().getRoll() + dRoll));

        return super.roll(roll);
    }

    public ParticleCompound() {
        super();

        this.particleOffset = Lists.newArrayList();
    }

    protected ParticleCompound(ConfigurationSection section) {
        super(section);

        this.particleOffset = new ArrayList<>();
        for (String subKey : section.getConfigurationSection("particles").getKeys(false)) {
            Particle loadedParticle = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "particles." + subKey), Particle.class);
            Vector offset = section.contains("particles." + subKey + ".offset") ?
                    ConfigTypeRegistry.getFromType(new ConfigEntry(section, "particles." + subKey + ".offset"), Vector.class) :
                    new Vector(0, 0, 0);

            addParticle(loadedParticle, offset);
        }
    }

    /**
     * Adds a particle with the given offset to the compound display
     *
     * @param particle the particle
     * @param offset the offset
     * @return the compound
     */
    public ParticleCompound addParticle(Particle particle, Vector offset) {
        if (this == particle)
            throw new IllegalArgumentException("Can't add self to particle compound!");

        particleOffset.add(new Pair<>(particle, offset));
        return this;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        for (Pair<Particle, Vector> pair : particleOffset) {
            Vector offset = pair.getB().clone();

            manipulateToAngles(offset);

            Location spawnAt = location.clone().add(offset);

            pair.getA().spawnAt(players, spawnAt);
        }
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.COMPOUND;
    }

    @Override
    public Map<String, Object> toPrimitive() {
        Map<String, Object> data = new HashMap<>();
        int count = 1;
        for (var pair : this.particleOffset) {
            Map<String, Object> particleData = ConfigTypeRegistry.toPrimitive(pair.getA()).unwrap();
            particleData.put("offset", ConfigTypeRegistry.toPrimitive(pair.getB()).getPrimitive());
            data.put("particle-" + count++, particleData);
        }
        Map<String, Object> particles = new HashMap<>();
        particles.put("particles", data);
        return particles;
    }
}
