package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;

/**
 * Represents a "line" of particles between two points.
 */
public class ParticleLine extends Particle {

    private final Vector endPointOffset;
    private final Vector startingPointOffset;

    public ParticleLine(Vector startingPointOffset, Vector endPointOffset) {
        this.startingPointOffset = startingPointOffset;

        this.endPointOffset = endPointOffset;
    }

    protected ParticleLine(ConfigurationSection section) {
        super(section);

        this.endPointOffset = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "end-point"), Vector.class);
        this.startingPointOffset = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "start-point"), Vector.class);
    }

    @Override
    public ParticleType getParticleType() {
        return ParticleType.LINE;
    }

    @Override
    public void spawnAt(Collection<Player> players, Location location) {
        Vector manipulatedEnd = manipulateToAngles(endPointOffset.clone());
        Vector manipulatedBeginning = manipulateToAngles(startingPointOffset.clone());

        Vector lineDrawVector = manipulatedEnd.clone().subtract(manipulatedBeginning).normalize().multiply(0.15D);

        Location particleLocation = location.clone().add(manipulatedBeginning);

        Vector finalEndPoint = manipulatedEnd.add(location.toVector());

        while (!particleLocation.toVector().isInSphere(finalEndPoint, 0.1)) {
            spawnParticle(players, particleLocation);

            particleLocation.add(lineDrawVector);
        }

        Location finalLocation = manipulatedBeginning.clone().add(manipulatedEnd).toLocation(location.getWorld());

        spawnParticle(players, finalLocation);
    }
}
