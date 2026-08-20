package com.golfing8.kcommon.util;

import com.golfing8.kcommon.struct.helper.exception.HelperExceptions;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.enums.EntityTaskResult;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * FoliaLib-backed scheduler bridge bound to a single {@link Plugin} instance.
 */
public final class FoliaSchedulers {
    private static final Map<Plugin, FoliaSchedulers> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Initializes and binds a scheduler bridge to the given plugin instance.
     *
     * @param plugin the plugin
     * @return the scheduler bridge for that plugin
     */
    public static @NotNull FoliaSchedulers initialize(@NotNull Plugin plugin) {
        return INSTANCES.computeIfAbsent(plugin, FoliaSchedulers::new);
    }

    /**
     * Gets the scheduler bridge bound to the given plugin instance.
     *
     * @param plugin the plugin
     * @return the bound scheduler bridge
     */
    public static @NotNull FoliaSchedulers of(@NotNull Plugin plugin) {
        FoliaSchedulers scheduler = INSTANCES.get(plugin);
        if (scheduler == null) {
            throw new IllegalStateException(String.format("No FoliaSchedulers instance is bound to plugin %s.", plugin.getName()));
        }
        return scheduler;
    }

    /**
     * Gets the scheduler bridge bound to the plugin that provides the given class.
     *
     * @param providingClass class from the target plugin classloader
     * @return the bound scheduler bridge
     */
    public static @NotNull FoliaSchedulers ofProvidingPlugin(@NotNull Class<?> providingClass) {
        return of(JavaPlugin.getProvidingPlugin(providingClass));
    }

    /**
     * Unbinds and shuts down the scheduler bridge for the given plugin.
     *
     * @param plugin the plugin
     */
    public static void shutdown(@NotNull Plugin plugin) {
        FoliaSchedulers schedulers = INSTANCES.remove(plugin);
        if (schedulers != null) {
            schedulers.close();
        }
    }

    @Getter
    private final FoliaLib foliaLib;

    private FoliaSchedulers(@NotNull Plugin plugin) {
        this.foliaLib = new FoliaLib(plugin);
    }

    /**
     * Cancels all scheduled tasks for this plugin scheduler bridge.
     */
    public void close() {
        this.foliaLib.getScheduler().cancelAllTasks();
    }

    /**
     * Checks whether the current thread owns the region containing the location.
     *
     * @param location the location to check
     * @return whether the current thread owns the region
     */
    public boolean isOwnedByCurrentRegion(@NotNull Location location) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(location);
    }

    /**
     * Checks whether the current thread owns the regions within the location's radius.
     *
     * @param location     the center location to check
     * @param squareRadius the radius in chunks
     * @return whether the current thread owns the regions
     */
    public boolean isOwnedByCurrentRegion(@NotNull Location location, int squareRadius) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(location, squareRadius);
    }

    /**
     * Checks whether the current thread owns the region containing the block.
     *
     * @param block the block to check
     * @return whether the current thread owns the region
     */
    public boolean isOwnedByCurrentRegion(@NotNull Block block) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(block);
    }

    /**
     * Checks whether the current thread owns the region containing the chunk.
     *
     * @param world  the world containing the chunk
     * @param chunkX the chunk X coordinate
     * @param chunkZ the chunk Z coordinate
     * @return whether the current thread owns the region
     */
    public boolean isOwnedByCurrentRegion(@NotNull World world, int chunkX, int chunkZ) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(world, chunkX, chunkZ);
    }

    /**
     * Checks whether the current thread owns the region containing the block coordinates.
     *
     * @param world  the world containing the block
     * @param blockX the block X coordinate
     * @param blockY the block Y coordinate
     * @param blockZ the block Z coordinate
     * @return whether the current thread owns the region
     */
    public boolean isOwnedByCurrentRegion(@NotNull World world, int blockX, int blockY, int blockZ) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(world, blockX, blockY, blockZ);
    }

    /**
     * Checks whether the current thread owns the region containing the entity.
     *
     * @param entity the entity to check
     * @return whether the current thread owns the region
     */
    public boolean isOwnedByCurrentRegion(@NotNull Entity entity) {
        return foliaLib.getScheduler().isOwnedByCurrentRegion(entity);
    }

    /**
     * Checks whether the current thread is the global tick thread.
     *
     * @return whether the current thread is the global tick thread
     */
    public boolean isGlobalTickThread() {
        return foliaLib.getScheduler().isGlobalTickThread();
    }

    /**
     * Runs a task on the next global tick.
     *
     * @param task task callback
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runNextTick(@NotNull Consumer<WrappedTask> task) {
        return foliaLib.getScheduler().runNextTick(task);
    }

    /**
     * Runs a task on the platform sync/global scheduler.
     *
     * @param runnable the task
     */
    public void runSync(@NotNull Runnable runnable) {
        if (foliaLib.getScheduler().isGlobalTickThread()) {
            runnable.run();
            return;
        }
        foliaLib.getScheduler().runNextTick(task -> runnable.run());
    }

    /**
     * Runs a task asynchronously.
     *
     * @param runnable the task
     */
    public void runAsync(@NotNull Runnable runnable) {
        foliaLib.getScheduler().runAsync(task -> runnable.run());
    }

    /**
     * Runs a task asynchronously with access to its wrapped task.
     *
     * @param task task callback
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAsync(@NotNull Consumer<WrappedTask> task) {
        return foliaLib.getScheduler().runAsync(task);
    }

    /**
     * Runs a delayed task on the platform sync/global scheduler.
     *
     * @param runnable   the task
     * @param delayTicks delay in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runLater(@NotNull Runnable runnable, long delayTicks) {
        return foliaLib.getScheduler().runLater(runnable, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed task with access to its wrapped task.
     *
     * @param task       task callback
     * @param delayTicks delay in ticks
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> task, long delayTicks) {
        return foliaLib.getScheduler().runLater(task, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed task using the supplied time unit.
     *
     * @param runnable task
     * @param delay    delay in the supplied unit
     * @param unit     delay unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runLater(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runLater(runnable, delay, unit);
    }

    /**
     * Runs a delayed task with access to its wrapped task using the supplied time unit.
     *
     * @param task  task callback
     * @param delay delay in the supplied unit
     * @param unit  delay unit
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runLater(@NotNull Consumer<WrappedTask> task, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runLater(task, delay, unit);
    }

    /**
     * Runs a delayed asynchronous task.
     *
     * @param runnable   the task
     * @param delayTicks delay in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runLaterAsync(@NotNull Runnable runnable, long delayTicks) {
        return foliaLib.getScheduler().runLaterAsync(runnable, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed asynchronous task with access to its wrapped task.
     *
     * @param task       task callback
     * @param delayTicks delay in ticks
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> task, long delayTicks) {
        return foliaLib.getScheduler().runLaterAsync(task, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed asynchronous task using the supplied time unit.
     *
     * @param runnable task
     * @param delay    delay in the supplied unit
     * @param unit     delay unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runLaterAsync(@NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runLaterAsync(runnable, delay, unit);
    }

    /**
     * Runs a delayed asynchronous task with access to its wrapped task using the supplied time unit.
     *
     * @param task  task callback
     * @param delay delay in the supplied unit
     * @param unit  delay unit
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runLaterAsync(@NotNull Consumer<WrappedTask> task, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runLaterAsync(task, delay, unit);
    }

    /**
     * Runs a repeating task on the platform sync/global scheduler.
     *
     * @param runnable    the task
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runTimer(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return foliaLib.getScheduler().runTimer(runnable, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating task with access to its wrapped task.
     *
     * @param task        task callback
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     */
    public void runTimer(@NotNull Consumer<WrappedTask> task, long delayTicks, long periodTicks) {
        foliaLib.getScheduler().runTimer(task, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating task using the supplied time unit.
     *
     * @param runnable task
     * @param delay    initial delay in the supplied unit
     * @param period   repeat period in the supplied unit
     * @param unit     delay and period unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runTimer(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runTimer(runnable, delay, period, unit);
    }

    /**
     * Runs a repeating task with access to its wrapped task using the supplied time unit.
     *
     * @param task   task callback
     * @param delay  initial delay in the supplied unit
     * @param period repeat period in the supplied unit
     * @param unit   delay and period unit
     */
    public void runTimer(@NotNull Consumer<WrappedTask> task, long delay, long period, @NotNull TimeUnit unit) {
        foliaLib.getScheduler().runTimer(task, delay, period, unit);
    }

    /**
     * Runs a repeating asynchronous task.
     *
     * @param runnable    the task
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runTimerAsync(@NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return foliaLib.getScheduler().runTimerAsync(runnable, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating asynchronous task with access to its wrapped task.
     *
     * @param task        task callback
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     */
    public void runTimerAsync(@NotNull Consumer<WrappedTask> task, long delayTicks, long periodTicks) {
        foliaLib.getScheduler().runTimerAsync(task, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating asynchronous task using the supplied time unit.
     *
     * @param runnable task
     * @param delay    initial delay in the supplied unit
     * @param period   repeat period in the supplied unit
     * @param unit     delay and period unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runTimerAsync(@NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runTimerAsync(runnable, delay, period, unit);
    }

    /**
     * Runs a repeating asynchronous task with access to its wrapped task using the supplied time unit.
     *
     * @param task   task callback
     * @param delay  initial delay in the supplied unit
     * @param period repeat period in the supplied unit
     * @param unit   delay and period unit
     */
    public void runTimerAsync(@NotNull Consumer<WrappedTask> task, long delay, long period, @NotNull TimeUnit unit) {
        foliaLib.getScheduler().runTimerAsync(task, delay, period, unit);
    }

    /**
     * Runs a delayed task on the scheduler owning the given location.
     *
     * @param location   the location owner
     * @param runnable   the task
     * @param delayTicks delay in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtLocationLater(@NotNull Location location, @NotNull Runnable runnable, long delayTicks) {
        return foliaLib.getScheduler().runAtLocationLater(location, runnable, Math.max(1L, delayTicks));
    }

    /**
     * Runs a task at a location.
     *
     * @param location location owner
     * @param task     task callback
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtLocation(@NotNull Location location, @NotNull Consumer<WrappedTask> task) {
        return foliaLib.getScheduler().runAtLocation(location, task);
    }

    /**
     * Runs a delayed task at a location with access to its wrapped task.
     *
     * @param location   location owner
     * @param task       task callback
     * @param delayTicks delay in ticks
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtLocationLater(@NotNull Location location, @NotNull Consumer<WrappedTask> task, long delayTicks) {
        return foliaLib.getScheduler().runAtLocationLater(location, task, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed task at a location using the supplied time unit.
     *
     * @param location location owner
     * @param runnable task
     * @param delay    delay in the supplied unit
     * @param unit     delay unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtLocationLater(@NotNull Location location, @NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtLocationLater(location, runnable, delay, unit);
    }

    /**
     * Runs a delayed task at a location with access to its wrapped task using the supplied time unit.
     *
     * @param location location owner
     * @param task     task callback
     * @param delay    delay in the supplied unit
     * @param unit     delay unit
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtLocationLater(@NotNull Location location, @NotNull Consumer<WrappedTask> task, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtLocationLater(location, task, delay, unit);
    }

    /**
     * Runs a repeating task on the scheduler owning the given location.
     *
     * @param location    the location owner
     * @param runnable    the task
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtLocationTimer(@NotNull Location location, @NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return foliaLib.getScheduler().runAtLocationTimer(location, runnable, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating task at a location with access to its wrapped task.
     *
     * @param location    location owner
     * @param task        task callback
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     */
    public void runAtLocationTimer(@NotNull Location location, @NotNull Consumer<WrappedTask> task, long delayTicks, long periodTicks) {
        foliaLib.getScheduler().runAtLocationTimer(location, task, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating task at a location using the supplied time unit.
     *
     * @param location location owner
     * @param runnable task
     * @param delay    initial delay in the supplied unit
     * @param period   repeat period in the supplied unit
     * @param unit     delay and period unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtLocationTimer(@NotNull Location location, @NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtLocationTimer(location, runnable, delay, period, unit);
    }

    /**
     * Runs a repeating task at a location with access to its wrapped task using the supplied time unit.
     *
     * @param location location owner
     * @param task     task callback
     * @param delay    initial delay in the supplied unit
     * @param period   repeat period in the supplied unit
     * @param unit     delay and period unit
     */
    public void runAtLocationTimer(@NotNull Location location, @NotNull Consumer<WrappedTask> task, long delay, long period, @NotNull TimeUnit unit) {
        foliaLib.getScheduler().runAtLocationTimer(location, task, delay, period, unit);
    }

    /**
     * Runs a delayed task on the scheduler owning the given entity.
     *
     * @param entity     the entity owner
     * @param runnable   the task
     * @param delayTicks delay in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityLater(@NotNull Entity entity, @NotNull Runnable runnable, long delayTicks) {
        return foliaLib.getScheduler().runAtEntityLater(entity, runnable, Math.max(1L, delayTicks));
    }

    /**
     * Runs a task on the scheduler owning an entity.
     *
     * @param entity entity owner
     * @param task   task callback
     * @return task result future
     */
    public @NotNull CompletableFuture<EntityTaskResult> runAtEntity(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task) {
        return foliaLib.getScheduler().runAtEntity(entity, task);
    }

    /**
     * Runs a task on the scheduler owning an entity, with a fallback if scheduling fails.
     *
     * @param entity   entity owner
     * @param task     task callback
     * @param fallback fallback task
     * @return task result future
     */
    public @NotNull CompletableFuture<EntityTaskResult> runAtEntityWithFallback(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, @NotNull Runnable fallback) {
        return foliaLib.getScheduler().runAtEntityWithFallback(entity, task, fallback);
    }

    /**
     * Runs a delayed entity task with a fallback if the entity retires.
     *
     * @param entity          entity owner
     * @param runnable        task
     * @param retiredFallback fallback task
     * @param delayTicks      delay in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityLater(@NotNull Entity entity, @NotNull Runnable runnable, @NotNull Runnable retiredFallback, long delayTicks) {
        return foliaLib.getScheduler().runAtEntityLater(entity, runnable, retiredFallback, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed entity task with access to its wrapped task.
     *
     * @param entity     entity owner
     * @param task       task callback
     * @param delayTicks delay in ticks
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtEntityLater(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, long delayTicks) {
        return foliaLib.getScheduler().runAtEntityLater(entity, task, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed entity task with a fallback if the entity retires.
     *
     * @param entity          entity owner
     * @param task            task callback
     * @param retiredFallback fallback task
     * @param delayTicks      delay in ticks
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtEntityLater(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, @NotNull Runnable retiredFallback, long delayTicks) {
        return foliaLib.getScheduler().runAtEntityLater(entity, task, retiredFallback, Math.max(1L, delayTicks));
    }

    /**
     * Runs a delayed entity task using the supplied time unit.
     *
     * @param entity   entity owner
     * @param runnable task
     * @param delay    delay in the supplied unit
     * @param unit     delay unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityLater(@NotNull Entity entity, @NotNull Runnable runnable, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtEntityLater(entity, runnable, delay, unit);
    }

    /**
     * Runs a delayed entity task with access to its wrapped task using the supplied time unit.
     *
     * @param entity entity owner
     * @param task   task callback
     * @param delay  delay in the supplied unit
     * @param unit   delay unit
     * @return completion future
     */
    public @NotNull CompletableFuture<Void> runAtEntityLater(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, long delay, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtEntityLater(entity, task, delay, unit);
    }

    /**
     * Runs a repeating task on the scheduler owning an entity.
     *
     * @param entity      entity owner
     * @param runnable    task
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delayTicks, long periodTicks) {
        return foliaLib.getScheduler().runAtEntityTimer(entity, runnable, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating entity task with a fallback if the entity retires.
     *
     * @param entity          entity owner
     * @param runnable        task
     * @param retiredFallback fallback task
     * @param delayTicks      initial delay in ticks
     * @param periodTicks     repeat period in ticks
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable runnable, @NotNull Runnable retiredFallback, long delayTicks, long periodTicks) {
        return foliaLib.getScheduler().runAtEntityTimer(entity, runnable, retiredFallback, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating entity task with access to its wrapped task.
     *
     * @param entity      entity owner
     * @param task        task callback
     * @param delayTicks  initial delay in ticks
     * @param periodTicks repeat period in ticks
     */
    public void runAtEntityTimer(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, long delayTicks, long periodTicks) {
        foliaLib.getScheduler().runAtEntityTimer(entity, task, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating entity task with a fallback if the entity retires.
     *
     * @param entity          entity owner
     * @param task            task callback
     * @param retiredFallback fallback task
     * @param delayTicks      initial delay in ticks
     * @param periodTicks     repeat period in ticks
     */
    public void runAtEntityTimer(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, @NotNull Runnable retiredFallback, long delayTicks, long periodTicks) {
        foliaLib.getScheduler().runAtEntityTimer(entity, task, retiredFallback, Math.max(1L, delayTicks), Math.max(1L, periodTicks));
    }

    /**
     * Runs a repeating entity task using the supplied time unit.
     *
     * @param entity   entity owner
     * @param runnable task
     * @param delay    initial delay in the supplied unit
     * @param period   repeat period in the supplied unit
     * @param unit     delay and period unit
     * @return wrapped scheduled task
     */
    public @NotNull WrappedTask runAtEntityTimer(@NotNull Entity entity, @NotNull Runnable runnable, long delay, long period, @NotNull TimeUnit unit) {
        return foliaLib.getScheduler().runAtEntityTimer(entity, runnable, delay, period, unit);
    }

    /**
     * Runs a repeating entity task with access to its wrapped task using the supplied time unit.
     *
     * @param entity entity owner
     * @param task   task callback
     * @param delay  initial delay in the supplied unit
     * @param period repeat period in the supplied unit
     * @param unit   delay and period unit
     */
    public void runAtEntityTimer(@NotNull Entity entity, @NotNull Consumer<WrappedTask> task, long delay, long period, @NotNull TimeUnit unit) {
        foliaLib.getScheduler().runAtEntityTimer(entity, task, delay, period, unit);
    }

    /**
     * Cancels a scheduled task.
     *
     * @param task task to cancel
     */
    public void cancelTask(@NotNull WrappedTask task) {
        foliaLib.getScheduler().cancelTask(task);
    }

    /**
     * Cancels all tasks scheduled through FoliaLib.
     */
    public void cancelAllTasks() {
        foliaLib.getScheduler().cancelAllTasks();
    }

    /**
     * Gets all tasks scheduled through this scheduler.
     *
     * @return scheduled tasks
     */
    public @NotNull List<WrappedTask> getAllTasks() {
        return foliaLib.getScheduler().getAllTasks();
    }

    /**
     * Gets all server tasks visible to this scheduler.
     *
     * @return server tasks
     */
    public @NotNull List<WrappedTask> getAllServerTasks() {
        return foliaLib.getScheduler().getAllServerTasks();
    }

    /**
     * Finds an online player by name.
     *
     * @param name player name
     * @return player, or null if not online
     */
    public @Nullable Player getPlayer(@NotNull String name) {
        return foliaLib.getScheduler().getPlayer(name);
    }

    /**
     * Finds an online player by exact name.
     *
     * @param name player name
     * @return player, or null if not online
     */
    public @Nullable Player getPlayerExact(@NotNull String name) {
        return foliaLib.getScheduler().getPlayerExact(name);
    }

    /**
     * Finds an online player by UUID.
     *
     * @param uuid player UUID
     * @return player, or null if not online
     */
    public @Nullable Player getPlayer(@NotNull UUID uuid) {
        return foliaLib.getScheduler().getPlayer(uuid);
    }

    /**
     * Wraps a platform task object.
     *
     * @param task platform task object
     * @return wrapped task
     */
    public @NotNull WrappedTask wrapTask(@NotNull Object task) {
        return foliaLib.getScheduler().wrapTask(task);
    }

    /**
     * Runs a supplier immediately if already region-owned; otherwise dispatches to the location scheduler and blocks for the result.
     *
     * @param location the location owner
     * @param supplier supplier to execute
     * @param <T>      supplied result type
     * @return supplied value
     */
    public <T> @NotNull T callAtLocationNow(@NotNull Location location, @NotNull Supplier<T> supplier) {
        if (foliaLib.getScheduler().isOwnedByCurrentRegion(location)) {
            return supplier.get();
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        foliaLib.getScheduler().runAtLocation(location, task -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.join();
    }

    /**
     * Runs a supplier immediately if already region-owned; otherwise dispatches to the location scheduler and blocks for the result.
     *
     * @param location the location owner
     * @param supplier supplier to execute
     * @param <T>      supplied result type
     * @return supplied value
     */
    public <T> @NotNull CompletableFuture<@NotNull T> callAtLocationAsync(@NotNull Location location, @NotNull Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (foliaLib.getScheduler().isOwnedByCurrentRegion(location)) {
            future.complete(supplier.get());
            return future;
        }

        foliaLib.getScheduler().runAtLocation(location, task -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // Log the exception if needed
                HelperExceptions.reportScheduler(throwable);
            }
        });
    }

    /**
     * Loops through a list of locations, running a task at each location and counting the number of successful results.
     *
     * @param locations    the locations
     * @param taskSupplier the task supplier
     * @return the number of successful results
     */
    public @NotNull CompletableFuture<Integer> forEachCountingResult(@NotNull Collection<Location> locations, @NotNull Function<Location, CompletableFuture<Integer>> taskSupplier) {
        return forEachCountingResult(locations, Function.identity(), taskSupplier);
    }

    /**
     * Loops through a list of subjects, running a task at each location and counting the number of successful results.
     *
     * @param subjects         the subjects
     * @param locationFunction function to get the location from a subject
     * @param taskSupplier     the task supplier
     * @return the number of successful results
     */
    public <T> @NotNull CompletableFuture<Integer> forEachCountingResult(@NotNull Collection<T> subjects, @NotNull Function<T, Location> locationFunction, @NotNull Function<T, CompletableFuture<Integer>> taskSupplier) {
        CompletableFuture<Integer> resultFuture = new CompletableFuture<>();
        AtomicInteger count = new AtomicInteger(0);
        AtomicInteger completedCount = new AtomicInteger(0);

        for (T subject : subjects) {
            Location location;
            try {
                location = locationFunction.apply(subject);
            } catch (Throwable throwable) {
                HelperExceptions.reportScheduler(throwable);
                if (completedCount.incrementAndGet() == subjects.size()) {
                    resultFuture.complete(count.get());
                }
                continue;
            }
            callAtLocationAsync(location, () -> {
                taskSupplier.apply(subject).thenAccept(success -> {
                    count.addAndGet(success);
                    if (completedCount.incrementAndGet() == subjects.size()) {
                        resultFuture.complete(count.get());
                    }
                }).exceptionally(ex -> {
                    if (completedCount.incrementAndGet() == subjects.size()) {
                        resultFuture.complete(count.get());
                    }
                    return null;
                });
                return null;
            });
        }

        return resultFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // Log the exception if needed
                HelperExceptions.reportScheduler(throwable);
            }
        });
    }

    /**
     * Loops through a list of locations, running a task at each location and returning the result of the first successful task.
     *
     * @param locations    the locations
     * @param taskSupplier the task supplier
     * @param <T>          the type of the result
     * @return a future that completes with the result of the first successful task, or completes exceptionally if all tasks fail
     */
    public <T> @NotNull CompletableFuture<T> mapFirstLocationTo(@NotNull Collection<Location> locations, @NotNull Function<Location, CompletableFuture<T>> taskSupplier) {
        return mapFirstLocationTo(locations, Function.identity(), taskSupplier);
    }

    /**
     * Loops through a list of subjects, running a task at each location and returning the result of the first successful task.
     * A task may return null to indicate failure, in which case the next task will be attempted. If all tasks fail, the returned future will complete with null.
     *
     * @param subjects         the subjects
     * @param locationFunction function to get the location from a subject
     * @param taskSupplier     the task supplier
     * @param <T>              the type of the result
     * @param <S>              the type of the subject
     * @return a future that completes with the result of the first successful task, or completes exceptionally if all tasks fail
     */
    public <T, S> @NotNull CompletableFuture<@Nullable T> mapFirstLocationTo(@NotNull Collection<S> subjects, @NotNull Function<S, Location> locationFunction, @NotNull Function<S, CompletableFuture<@Nullable T>> taskSupplier) {
        CompletableFuture<T> resultFuture = new CompletableFuture<>();
        AtomicBoolean found = new AtomicBoolean(false);

        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (S subject : subjects) {
            if (found.get()) {
                break;
            }
            Location location;
            try {
                location = locationFunction.apply(subject);
            } catch (Throwable throwable) {
                if (found.compareAndSet(false, true)) {
                    resultFuture.completeExceptionally(throwable);
                }
                continue;
            }
            futures.add(callAtLocationAsync(location, () -> {
                CompletableFuture<@Nullable T> apply = taskSupplier.apply(subject);
                if (apply == null) {
                    resultFuture.completeExceptionally(new NullPointerException("taskSupplier returned null"));
                    return null;
                }

                apply.thenAccept(result -> {
                    if (result == null)
                        return;

                    if (!found.getAndSet(true)) {
                        resultFuture.complete(result);
                    }
                }).exceptionally(ex -> {
                    if (!found.getAndSet(true)) {
                        resultFuture.completeExceptionally(ex);
                    }
                    return null;
                });
                return null;
            }));
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
            if (!found.get()) {
                resultFuture.complete(null);
            }
        });

        return resultFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // Log the exception if needed
                HelperExceptions.reportScheduler(throwable);
            }
        });
    }

    /**
     * Maps all of the given subjects to a list of something else
     *
     * @param subjects the subjects
     * @param locationFunction function to get the location from a subject
     * @param taskSupplier the task supplier
     * @return a future that completes with a list of the results
     * @param <T> the type of the result
     * @param <S> the type of the subject
     */
    public <T, S> @NotNull CompletableFuture<List<T>> mapSubjectsTo(@Nullable Collection<S> subjects, @NotNull Function<S, Location> locationFunction, @NotNull Function<S, CompletableFuture<@Nullable T>> taskSupplier) {
        if (subjects == null || subjects.isEmpty()) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        List<T> results = new ArrayList<>(Collections.nCopies(subjects.size(), null));
        CompletableFuture<List<T>> resultFuture = new CompletableFuture<>();
        AtomicInteger remaining = new AtomicInteger(subjects.size());
        AtomicBoolean completed = new AtomicBoolean(false);

        int index = 0;
        for (S subject : subjects) {
            final int currentIndex = index++;

            Location location;
            try {
                location = locationFunction.apply(subject);
            } catch (Throwable throwable) {
                if (completed.compareAndSet(false, true)) {
                    resultFuture.completeExceptionally(throwable);
                }
                continue;
            }

            try {
                callAtLocationAsync(location, () -> {
                    if (completed.get()) {
                        return null;
                    }

                    CompletableFuture<@Nullable T> taskFuture;
                    try {
                        taskFuture = taskSupplier.apply(subject);
                    } catch (Throwable throwable) {
                        if (completed.compareAndSet(false, true)) {
                            resultFuture.completeExceptionally(throwable);
                        }
                        return null;
                    }

                    if (taskFuture == null) {
                        if (completed.compareAndSet(false, true)) {
                            resultFuture.completeExceptionally(new NullPointerException("taskSupplier returned null"));
                        }
                        return null;
                    }

                    taskFuture.whenComplete((result, throwable) -> {
                        if (completed.get()) {
                            return;
                        }

                        if (throwable != null) {
                            if (completed.compareAndSet(false, true)) {
                                resultFuture.completeExceptionally(throwable);
                            }
                            return;
                        }

                        results.set(currentIndex, result);
                        if (remaining.decrementAndGet() == 0 && completed.compareAndSet(false, true)) {
                            resultFuture.complete(results);
                        }
                    });

                    return null;
                }).exceptionally(throwable -> {
                    if (completed.compareAndSet(false, true)) {
                        resultFuture.completeExceptionally(throwable);
                    }
                    return null;
                });
            } catch (Throwable throwable) {
                if (completed.compareAndSet(false, true)) {
                    resultFuture.completeExceptionally(throwable);
                }
            }
        }

        return resultFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                // Log the exception if needed
                HelperExceptions.reportScheduler(throwable);
            }
        });
    }

    /**
     * Loops over each world and maps all subjects to a list of something else, returning a combined list of all results.
     *
     * @param worldToSubjectFunction the world to subject function
     * @param locationFunction the location function for each subject
     * @param taskSupplier the task supplier
     * @return the future of lists
     * @param <T> the type of the result
     * @param <S> the type of the subject
     */
    public <T, S> CompletableFuture<List<T>> forEachWorldMapSubjectsTo(Function<World, Collection<S>> worldToSubjectFunction, Function<S, Location> locationFunction, Function<S, CompletableFuture<@Nullable T>> taskSupplier) {
        List<CompletableFuture<List<T>>> futures = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            Collection<S> subjects = worldToSubjectFunction.apply(world);
            if (subjects != null && !subjects.isEmpty()) {
                futures.add(mapSubjectsTo(subjects, locationFunction, taskSupplier));
            }
        }
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .flatMap(future -> future.join().stream())
                        .collect(Collectors.toList()));
    }

    /**
     * Runs a task immediately if already region-owned; otherwise dispatches to the location scheduler and waits.
     *
     * @param location the location owner
     * @param runnable the task
     */
    public void runAtLocationNow(@NotNull Location location, @NotNull Runnable runnable) {
        callAtLocationNow(location, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Runs a supplier immediately if already entity-owned; otherwise dispatches to the entity scheduler and blocks for the result.
     *
     * @param entity   the entity owner
     * @param supplier supplier to execute
     * @param <T>      supplied result type
     * @return supplied value
     */
    public <T> @NotNull T callAtEntityNow(@NotNull Entity entity, @NotNull Supplier<T> supplier) {
        if (foliaLib.getScheduler().isOwnedByCurrentRegion(entity)) {
            return supplier.get();
        }

        CompletableFuture<T> future = new CompletableFuture<>();
        foliaLib.getScheduler().runAtEntity(entity, task -> {
            try {
                future.complete(supplier.get());
            } catch (Throwable throwable) {
                future.completeExceptionally(throwable);
            }
        });
        return future.join();
    }

    /**
     * Runs a task immediately if already entity-owned; otherwise dispatches to the entity scheduler and waits.
     *
     * @param entity   the entity owner
     * @param runnable the task
     */
    public void runAtEntityNow(@NotNull Entity entity, @NotNull Runnable runnable) {
        callAtEntityNow(entity, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Runs a supplier in the chunk's owning region, blocking until the result is available.
     *
     * @param world    the world
     * @param chunkX   chunk X
     * @param chunkZ   chunk Z
     * @param supplier supplier to execute
     * @param <T>      supplied result type
     * @return supplied value
     */
    public <T> @NotNull T callAtChunkNow(@NotNull World world, int chunkX, int chunkZ, @NotNull Supplier<T> supplier) {
        if (foliaLib.getScheduler().isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
            return supplier.get();
        }

        Location location = new Location(world, chunkX << 4, 0, chunkZ << 4);
        return callAtLocationNow(location, supplier);
    }

    /**
     * Teleports an entity using FoliaLib's platform-aware async teleport support.
     *
     * @param entity   entity to teleport
     * @param location destination
     * @return completion future with success state
     */
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location) {
        return foliaLib.getScheduler().teleportAsync(entity, location);
    }

    /**
     * Teleports an entity using FoliaLib's platform-aware async teleport support.
     *
     * @param entity   entity to teleport
     * @param location destination
     * @param cause    optional teleport cause
     * @return completion future with success state
     */
    public @NotNull CompletableFuture<Boolean> teleportAsync(@NotNull Entity entity, @NotNull Location location, @Nullable PlayerTeleportEvent.TeleportCause cause) {
        if (cause == null) {
            return foliaLib.getScheduler().teleportAsync(entity, location);
        }
        return foliaLib.getScheduler().teleportAsync(entity, location, cause);
    }
}
