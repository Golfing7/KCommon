package com.golfing8.kcommon.util.block;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class BlockSelectionHelper implements Listener {
    private final Promise<@Nullable Location> resultPromise;
    private final Player player;
    private final @Nullable BukkitTask timeoutTask;

    public BlockSelectionHelper(Player player, int timeoutTicks) {
        this.player = player;
        this.resultPromise = Promise.empty();

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

    public BlockSelectionHelper(Player player) {
        this(player, 1200);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        if (event.getPlayer() != player)
            return;

        complete(null);
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {
        if (event.getPlayer() != player)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        complete(event.getClickedBlock().getLocation());
        event.setCancelled(true);
    }

    private void complete(@Nullable Location input) {
        if (resultPromise.isDone())
            return;

        resultPromise.supply(input);
        Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
            HandlerList.unregisterAll(this);
        });
        if (timeoutTask != null)
            timeoutTask.cancel();
    }
}
