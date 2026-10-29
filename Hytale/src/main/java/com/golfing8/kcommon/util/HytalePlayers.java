package com.golfing8.kcommon.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Helpers for working with Hytale players, whose components may only be touched on their world's thread.
 */
public final class HytalePlayers {
    private HytalePlayers() {
    }

    /**
     * Runs the action with the player's entity on their world's thread, right away if already on it.
     *
     * @param player the player.
     * @param action the action.
     * @return a future of true once the action ran, or false if the player had no entity in a world.
     */
    public static CompletableFuture<Boolean> runOnWorld(PlayerRef player, Consumer<Ref<EntityStore>> action) {
        UUID worldId = player.getWorldUuid();
        World world = worldId == null ? null : Universe.get().getWorld(worldId);
        if (world == null)
            return CompletableFuture.completedFuture(false);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                // The reference is null or invalid while the player moves between worlds.
                Ref<EntityStore> ref = player.getReference();
                if (ref == null || !ref.isValid()) {
                    future.complete(false);
                    return;
                }
                action.accept(ref);
                future.complete(true);
            } catch (Throwable thr) {
                future.completeExceptionally(thr);
            }
        };
        if (world.isInThread())
            task.run();
        else
            world.execute(task);
        return future;
    }
}
