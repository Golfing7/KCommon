package com.golfing8.kcommon.nms.event;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockEvent;

public class PreSpawnSpawnerEvent extends BlockEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private Location location;
    @Getter
    private EntityType type;

    private boolean cancelled = false;

    public PreSpawnSpawnerEvent(Block theBlock, EntityType type) {
        super(theBlock);
        location = theBlock.getLocation();
        this.type = type;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }
}
