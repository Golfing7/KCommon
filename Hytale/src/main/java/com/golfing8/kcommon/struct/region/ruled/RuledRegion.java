package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.struct.region.Region;
import com.hypixel.hytale.math.vector.Location;
import org.joml.Vector3d;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A region that contains certain rules that are enforced.
 * <p>
 * These rules may be something like:
 * </p>
 * <ol>
 * <li>Players may not break blocks</li>
 * <li>Players may not place blocks</li>
 * <li>A command whitelist/blacklist</li>
 * <li>Whitelisted/blacklisted entry</li>
 * </ol>
 */
@RequiredArgsConstructor
public class RuledRegion implements Region {
    /**
     * The enforcing plugin is used to start tasks/create listeners
     */
    /**
     * The region backing this ruled region.
     */
    private final Region backingRegion;
    /**
     * The rules of this region.
     */
    private final List<RegionRule> rules = new ArrayList<>();

    /**
     * Starts all rules attached to this region.
     */
    /**
     * Adds a rule to this region. Rules start when {@link #start()} is called.
     *
     * @param rule the rule.
     * @return this region.
     */
    public RuledRegion addRule(RegionRule rule) {
        rules.add(rule);
        return this;
    }

    @Override
    public String getWorld() {
        return backingRegion.getWorld();
    }

    /**
     * Starts enforcing every rule of this region.
     */
    public void start() {
        for (RegionRule regionRule : rules) {
            regionRule.register(this);
        }
    }

    /**
     * Shuts down this region and deletes all attached regions.
     */
    public void shutdown() {
        for (RegionRule regionRule : rules) {
            regionRule.shutdown();
        }
    }

    @Override
    public Position getCenter() {
        return backingRegion.getCenter();
    }

    @Override
    public double getMaximumXValue() {
        return backingRegion.getMaximumXValue();
    }

    @Override
    public double getMinimumXValue() {
        return backingRegion.getMinimumXValue();
    }

    @Override
    public double getMaximumYValue() {
        return backingRegion.getMaximumYValue();
    }

    @Override
    public double getMinimumYValue() {
        return backingRegion.getMinimumYValue();
    }

    @Override
    public double getMaximumZValue() {
        return backingRegion.getMaximumZValue();
    }

    @Override
    public double getMinimumZValue() {
        return backingRegion.getMinimumZValue();
    }

    @Override
    public double getVolume() {
        return backingRegion.getVolume();
    }

    @Override
    public double getDistance(Vector3d vector) {
        return backingRegion.getDistance(vector);
    }

    @Override
    public double getDistance(Location location) {
        return backingRegion.getDistance(location);
    }

    /**
     * Clones the backing region and grows it.
     * Note that region rules do NOT get cloned.
     *
     * @param toGrow the grown region.
     * @return the grown region.
     */
    @Override
    public Region grow(double toGrow) {
        return new RuledRegion(this.backingRegion.grow(toGrow));
    }

    @Override
    public Region shift(Vector3d offset) {
        return new RuledRegion(this.backingRegion.shift(offset));
    }

    @Override
    public Region withWorld(String world) {
        return new RuledRegion(this.backingRegion.withWorld(world));
    }

    @Override
    public boolean isPositionWithin(Vector3d vector) {
        return backingRegion.isPositionWithin(vector);
    }

    @Override
    public boolean overlapsWith(Region region) {
        return backingRegion.overlapsWith(region);
    }

    @Override
    public Vector3d getRandomPosition() {
        return backingRegion.getRandomPosition();
    }

    @NotNull
    @Override
    public Iterator<Position> iterator() {
        return backingRegion.iterator();
    }

    /**
     * The listener used to control all
     */
}
