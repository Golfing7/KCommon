package com.golfing8.kcommon.util.chat;

import com.golfing8.kcommon.KCommon;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Used to capture chat input for use.
 */
@Getter
public class ChatInputHelper implements Listener {
    /** The future of input. Completed with null if the player times out or disconnects */
    private final CompletableFuture<@Nullable String> result;
    private final Player player;
    private final @Nullable BukkitTask timeoutTask;

    public ChatInputHelper(Player player, int timeoutTicks) {
        this.player = player;
        this.result = new CompletableFuture<>();

        // Register
        Bukkit.getPluginManager().registerEvents(this, KCommon.getInstance());
        if (timeoutTicks > 0) {
            this.timeoutTask = Bukkit.getScheduler().runTaskLater(KCommon.getInstance(), () -> {
                complete(null);
            }, timeoutTicks);
        } else {
            this.timeoutTask = null;
        }
    }

    public ChatInputHelper(Player player) {
        this(player, 1200);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        complete(null);
    }

    // If the player enters a command instead of input, don't capture it and just let them go.
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        complete(null);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        event.setCancelled(true);
        complete(message);
    }

    private void complete(@Nullable String input) {
        if (result.isDone())
            return;

        result.complete(input);
        Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
            HandlerList.unregisterAll(this);
        });
        if (timeoutTask != null)
            timeoutTask.cancel();
    }
}
