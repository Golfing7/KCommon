package com.golfing8.kcommon.struct.particle;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

import java.util.Map;

/**
 * Abstract class used for the methods in any "function" particle line.
 */
public abstract class ParticleFunction extends Particle{
    //The amplitude of the function. (Note, for HIGH values, please provide a small an Interval with small dx).
    @Getter
    protected double amplitude = 1.0D;
    public ParticleFunction amplitude(double amplitude)
    {
        this.amplitude = amplitude;
        return this;
    }

    //If we should "smart fill".
    //This entails taking two points that are far away and connecting them with a line, not more particles.
    //This is useful for exponential functions, or things like 1/x.
    @Getter
    protected boolean smartFill = true;
    public ParticleFunction smartFill(boolean smartFill)
    {
        this.smartFill = smartFill;
        return this;
    }
    //If two particles are not within this distance, we smart fill.
    @Getter
    protected double smartFillThreshold = 0.2D;
    public ParticleFunction smartFillThreshold(double smartFillThreshold)
    {
        this.smartFillThreshold = smartFillThreshold;
        return this;
    }

    //This maxes out our smart fill, so we don't get absurdly long distances filled.
    @Getter
    protected double smartFillMaxThreshold = 3.0D;
    public ParticleFunction smartFillMaxThreshold(double smartFillMaxThreshold)
    {
        this.smartFillMaxThreshold = smartFillMaxThreshold;
        return this;
    }

    public ParticleFunction() {
        super();
    }

    protected ParticleFunction(ConfigurationSection section) {
        super(section);

        this.amplitude = section.getDouble("amplitude", 1.0D);
        this.smartFill = section.getBoolean("smart-fill", true);
    }

    @Override
    public Map<String, Object> toPrimitive() {
        throw new IllegalArgumentException("Cannot convert function particle to primitive");
    }

    protected void smartFillLocations(Location locOne, Location locTwo){
        //We MUST limit the height, otherwise, we may literally iterate infinitely.
        if(locOne.getY() < 0)locOne.setY(0);
        else if(locOne.getY() > 256)locOne.setY(256);
        if(locTwo.getY() < 0)locTwo.setY(0);
        else if(locTwo.getY() > 256)locTwo.setY(256);

        new ParticleLine(new Vector(), new Vector(locTwo.getX() - locOne.getX(), locTwo.getY() - locOne.getY(), locTwo.getZ() - locOne.getZ()))
                .from(getFrom())
                .to(getTo())
                .spawnAt(locOne);
    }

    protected boolean isLocationValid(Location location)
    {
        return !(location.getY() < 0) && !(location.getY() > 256);
    }
}
