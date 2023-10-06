package com.golfing8.kcommon.struct.particle;

import com.golfing8.kcommon.struct.Pair;
import com.google.common.collect.Lists;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class ParticleCompound extends Particle{
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

    public ParticleCompound addParticle(Particle particle, Vector offset){
        if(this == particle)
            throw new IllegalArgumentException("Can't add self to particle compound!");

        particleOffset.add(new Pair<>(particle, offset));
        return this;
    }

    @Override
    public void spawnAt(Location location) {
        for(Pair<Particle, Vector> pair : particleOffset)
        {
            Vector offset = pair.getB().clone();

            manipulateToAngles(offset);

            Location spawnAt = location.clone().add(offset);

            pair.getA().spawnAt(spawnAt);
        }
    }
}
