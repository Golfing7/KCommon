package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.struct.region.Region;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
    /** The enforcing plugin is used to start tasks/create listeners */
    @Getter
    private final Plugin enforcer;
    /** The region backing this ruled region. */
    private final Region backingRegion;
    /** The rules of this region. */
    private List<RegionRule> rules = new ArrayList<>();

    /**
     * Starts all rules attached to this region.
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
    public BlockVector getCenter() {
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
    public double getDistance(Vector vector) {
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
        return new RuledRegion(this.enforcer, this.backingRegion.grow(toGrow));
    }

    @Override
    public Region shift(Vector offset) {
        return new RuledRegion(this.enforcer, this.backingRegion.shift(offset));
    }

    @Override
    public Region withWorld(World world) {
        return new RuledRegion(this.enforcer, this.backingRegion.withWorld(world));
    }

    @Override
    public boolean isPositionWithin(Vector vector) {
        return backingRegion.isPositionWithin(vector);
    }

    @Override
    public boolean overlapsWith(Region region) {
        return backingRegion.overlapsWith(region);
    }

    @Override
    public Vector getRandomPosition() {
        return backingRegion.getRandomPosition();
    }

    @NotNull
    @Override
    public Iterator<BlockVector> iterator() {
        return backingRegion.iterator();
    }

    /**
     * The listener used to control all
     */
    private static class RegionListener implements Listener {

    }
}
