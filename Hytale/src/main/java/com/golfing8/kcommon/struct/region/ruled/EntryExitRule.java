package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Tells the plugin when players enter or leave the region.
 * <p>
 * Hytale has no cancellable move event, so this rule can't stop movement the way the Bukkit rule does.
 * Use OrbisGuard's entry and exit flags to block players.
 * </p>
 */
@Builder
public class EntryExitRule extends RegionRule {
    /**
     * Called when a player enters the region.
     */
    private Consumer<PlayerRef> onEnter;
    /**
     * Called when a player leaves the region.
     */
    private Consumer<PlayerRef> onExit;
    /**
     * How often player positions are checked, in Minecraft ticks. Defaults to 5.
     */
    @Builder.Default
    private long checkPeriodTicks = 5;
    private final Set<UUID> inside = new HashSet<>();

    @Override
    protected void startEnforcing(CompositeTerminable enforcement) {
        bind(Platform.get().runTimerAsync(this::check, checkPeriodTicks, checkPeriodTicks));
    }

    @Override
    protected void stopEnforcing() {
        inside.clear();
    }

    private synchronized void check() {
        Set<UUID> online = new HashSet<>();
        for (PlayerRef player : Universe.get().getPlayers()) {
            online.add(player.getUuid());
            boolean isIn = getRegion().isWithin(player);
            if (isIn && inside.add(player.getUuid())) {
                if (onEnter != null)
                    onEnter.accept(player);
            } else if (!isIn && inside.remove(player.getUuid())) {
                if (onExit != null)
                    onExit.accept(player);
            }
        }
        inside.retainAll(online);
    }
}
