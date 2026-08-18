package com.golfing8.kcommon.util.block;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Captures block selection for a player.
 */
@Getter
public class BlockSelectionHelper implements Listener, Terminable {
    private final Promise<@Nullable Location> resultPromise;
    private final Player player;
    private final @Nullable WrappedTask timeoutTask;

    public BlockSelectionHelper(Player player, int timeoutTicks) {
        this.player = player;
        this.resultPromise = Promise.empty();

        // Register
        Bukkit.getPluginManager().registerEvents(this, KCommon.getInstance());
        if (timeoutTicks > 0) {
            this.timeoutTask = FoliaSchedulers.of(KCommon.getInstance()).runAtEntityLater(player, () -> {
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

    @Override
    public void close() {
        complete(null);
    }

    private void complete(@Nullable Location input) {
        if (resultPromise.isDone())
            return;

        resultPromise.supply(input);
        FoliaSchedulers.of(KCommon.getInstance()).runAtEntityLater(player, () -> {
            HandlerList.unregisterAll(this);
        }, 1L);
        if (timeoutTask != null)
            timeoutTask.cancel();
    }
}
