package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.struct.region.Region;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
 * <ol>
 * <li>Players may not break blocks</li>
 * <li>Players may not place blocks</li>
 * <li>A command whitelist/blacklist</li>
 * <li>Whitelisted/blacklisted entry</li>
 * </ol>
 *
 * </p>
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
    public boolean isPositionWithin(Vector vector) {
        return backingRegion.isPositionWithin(vector);
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
