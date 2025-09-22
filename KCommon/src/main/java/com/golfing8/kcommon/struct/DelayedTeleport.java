package com.golfing8.kcommon.struct;

import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * A class for performing a delayed teleport.
 */
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DelayedTeleport implements Listener {
    /** The player being teleported */
    @Getter
    private final Player player;
    /** The delay in ticks */
    @Getter
    private final int delayTicks;
    /** The destination for the player */
    private final Location destination;
    public Location getDestination() {
        return destination.clone();
    }
    /** The plugin that registered this */
    @Getter
    private final Plugin plugin;
    /** Called when this teleport succeeds */
    private final @Nullable Runnable onSuccess;
    /** Called when this teleport fails */
    private final @Nullable Runnable onFailure;
    /** The message to send the player on success */
    private final @Nullable Message successMessage;
    /** The message to send the player on failure */
    private final @Nullable Message failureMessage;
    /** The task responsible for teleporting the player */
    private BukkitTask teleportTask;
    private boolean done;
    private final CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

    /**
     * Gets a promise of the result
     *
     * @return the result
     */
    public Promise<Boolean> getResult() {
        return Promise.wrapFuture(resultFuture);
    }

    /**
     * Performs the teleportation
     * @return true if the teleport succeeded
     */
    public boolean teleport() {
        if (done)
            return false;

        done = true;
        HandlerList.unregisterAll(this);
        teleportTask.cancel();
        if (onSuccess != null)
            onSuccess.run();
        if (successMessage != null)
            successMessage.send(player);

        boolean teleport = player.teleport(destination);
        resultFuture.complete(teleport);
        return teleport;
    }

    /**
     * Cancels this teleportation
     */
    public void cancel() {
        if (done)
            return;

        done = true;
        HandlerList.unregisterAll(this);
        teleportTask.cancel();
        if (onFailure != null)
            onFailure.run();
        if (failureMessage != null)
            failureMessage.send(player);
        resultFuture.complete(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (player != event.getPlayer())
            return;

        cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (player != event.getPlayer())
            return;

        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockY() != event.getTo().getBlockY() || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
            cancel();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (player != event.getEntity())
            return;

        cancel();
    }

    public static class Builder {
        private Player player;
        private int delayTicks;
        private Location destination;
        private Runnable onSuccess;
        private Runnable onFailure;
        private Message successMessage;
        private Message failureMessage;

        public static Builder builder(Player player, int delayTicks, Location destination) {
            Preconditions.checkNotNull(player, "Player is null");
            Preconditions.checkArgument(delayTicks >= 0, "Delay ticks is negative");
            Preconditions.checkNotNull(destination, "Destination is null");
            Preconditions.checkArgument(destination.getWorld() != null, "Destination world is null");

            Builder builder = new Builder();
            builder.player = player;
            builder.delayTicks = delayTicks;
            builder.destination = destination.clone();
            return builder;
        }

        public Builder onSuccess(Runnable onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        public Builder onFailure(Runnable onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        public Builder successMessage(Message successMessage) {
            this.successMessage = successMessage;
            return this;
        }

        public Builder failureMessage(Message failureMessage) {
            this.failureMessage = failureMessage;
            return this;
        }

        public DelayedTeleport build(Plugin plugin) {
            Preconditions.checkNotNull(plugin, "Plugin is null");

            DelayedTeleport delayedTeleport = new DelayedTeleport(player, delayTicks, destination, plugin, onSuccess, onFailure, successMessage, failureMessage);
            plugin.getServer().getPluginManager().registerEvents(delayedTeleport, plugin);
            delayedTeleport.teleportTask = Bukkit.getScheduler().runTaskLater(plugin, delayedTeleport::teleport, delayTicks);
            return delayedTeleport;
        }
    }
}
