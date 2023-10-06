package com.golfing8.kcommon.struct.particle;

import org.bukkit.Location;
import org.bukkit.util.Vector;

/**
 * Represents a "line" of particles between two points.
 */
public class ParticleLine extends Particle{

    private final Vector endPointOffset;
    private final Vector startingPointOffset;

    public ParticleLine(Vector startingPointOffset, Vector endPointOffset)
    {
        this.startingPointOffset = startingPointOffset;

        this.endPointOffset = endPointOffset;
    }

    @Override
    public void spawnAt(Location location) {
        Vector manipulatedEnd = manipulateToAngles(endPointOffset.clone());
        Vector manipulatedBeginning = manipulateToAngles(startingPointOffset.clone());

        Vector lineDrawVector = manipulatedEnd.clone().subtract(manipulatedBeginning).normalize().multiply(0.15D);

        Location particleLocation = location.clone().add(manipulatedBeginning);

        Vector finalEndPoint = manipulatedEnd.add(location.toVector());

        while(!particleLocation.toVector().isInSphere(finalEndPoint, 0.1))
        {
            spawnParticle(particleLocation);

            particleLocation.add(lineDrawVector);
        }

        Location finalLocation = manipulatedBeginning.clone().add(manipulatedEnd).toLocation(location.getWorld());

        spawnParticle(finalLocation);
    }
}
