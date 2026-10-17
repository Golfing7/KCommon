package com.golfing8.kcommon.struct;

import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.golfing8.kcommon.util.StringUtil;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

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

    /**
     * Gets a clone of the destination location
     *
     * @return the destination
     */
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
    /** The countdown task */
    private final @Nullable IntConsumer onCountdown;
    /** The message to send the player on success */
    private final @Nullable Message successMessage;
    /** The message to send the player on failure */
    private final @Nullable Message failureMessage;
    /** The message to send when counting down */
    private final @Nullable Message countdownMessage;
    /** The task responsible for teleporting the player */
    private WrappedTask teleportTask;
    /** The countdown task */
    private WrappedTask countdownTask;
    /** The total amount of ticks that have elapsed for the entity */
    private int ticks;
    private boolean done;
    private final CompletableFuture<Boolean> resultFuture = new CompletableFuture<>();

    private void onCountdownTask() {
        int tickDelta = delayTicks - ticks++;
        if (tickDelta < 0) {
            countdownTask.cancel();
            return;
        }

        if (onCountdown != null) {
            onCountdown.accept(tickDelta);
        }

        if (countdownMessage != null && tickDelta > 0 && tickDelta % 20 == 0) {
            countdownMessage.send(player, "SECONDS", tickDelta / 20, "TIME", StringUtil.timeFormatted(tickDelta / 20, true));
        }
    }

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

        FoliaSchedulers.of(plugin).teleportAsync(player, destination, PlayerTeleportEvent.TeleportCause.PLUGIN).thenAccept(resultFuture::complete);
        return true;
    }

    /**
     * Cancels this teleportation
     */
    public void cancel() {
        if (done)
            return;

        done = true;
        HandlerList.unregisterAll(this);
        countdownTask.cancel();
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

    /**
     * A builder for delayed teleports
     */
    public static class Builder {
        private Player player;
        private int delayTicks;
        private Location destination;
        private Runnable onSuccess;
        private Runnable onFailure;
        private IntConsumer onCountdown;
        private Message successMessage;
        private Message failureMessage;
        private Message countdownMessage;

        /**
         * Create a builder with the given arguments
         *
         * @param player the player
         * @param delayTicks the delay in ticks
         * @param destination the destination location
         * @return a builder
         */
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

        /**
         * Set the success task
         *
         * @param onSuccess the task
         * @return this
         */
        public Builder onSuccess(Runnable onSuccess) {
            this.onSuccess = onSuccess;
            return this;
        }

        /**
         * Set the failure task
         *
         * @param onFailure the task
         * @return this
         */
        public Builder onFailure(Runnable onFailure) {
            this.onFailure = onFailure;
            return this;
        }

        /**
         * Sets the countdown task.
         * The countdown task runs once per tick until the teleport is done, providing the time remaining.
         *
         * @param onCountdown the task to run for countdown
         * @return this
         */
        public Builder onCountdown(IntConsumer onCountdown) {
            this.onCountdown = onCountdown;
            return this;
        }

        /**
         * Set the success message
         *
         * @param successMessage the message
         * @return this
         */
        public Builder successMessage(Message successMessage) {
            this.successMessage = successMessage;
            return this;
        }

        /**
         * Set the failure message
         *
         * @param failureMessage the message
         * @return this
         */
        public Builder failureMessage(Message failureMessage) {
            this.failureMessage = failureMessage;
            return this;
        }

        /**
         * Sets the countdown message. Sent once every second with a SECONDS
         * placeholder for time
         *
         * @param countdownMessage the countdown message
         * @return this
         */
        public Builder countdownMessage(Message countdownMessage) {
            this.countdownMessage = countdownMessage;
            return this;
        }

        /**
         * Builds the instance with the given plugin as the backer
         *
         * @param plugin the plugin
         * @return the instance
         */
        public DelayedTeleport build(Plugin plugin) {
            Preconditions.checkNotNull(plugin, "Plugin is null");

            DelayedTeleport delayedTeleport = new DelayedTeleport(player, delayTicks, destination, plugin, onSuccess, onFailure, onCountdown, successMessage, failureMessage, countdownMessage);
            plugin.getServer().getPluginManager().registerEvents(delayedTeleport, plugin);
            delayedTeleport.teleportTask = FoliaSchedulers.of(plugin).runAtEntityLater(player, delayedTeleport::teleport, delayTicks);
            delayedTeleport.countdownTask = FoliaSchedulers.of(plugin).runAtEntityTimer(player, delayedTeleport::onCountdownTask, 0, 1);

            return delayedTeleport;
        }
    }
}
