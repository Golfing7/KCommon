package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.struct.item.FancyItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Drop for item stacks.
 */
@Getter
public class ItemDrop extends Drop<ItemStack> {
    private Map<String, ItemStackBuilder> items;
    private boolean giveDirectly;
    private boolean fancyDrop;
    private boolean playerLocked;
    public ItemDrop(double chance, @Nullable String displayName, Map<String, ItemStackBuilder> items, boolean giveDirectly, boolean fancyDrop, boolean playerLocked) {
        super(chance, displayName);
        this.items = items;
        this.giveDirectly = giveDirectly;
        this.fancyDrop = fancyDrop;
        this.playerLocked = playerLocked;
    }

    @Override
    public List<ItemStack> getDrop() {
        return items.values().stream().map(ItemStackBuilder::buildFromTemplate).collect(Collectors.toList());
    }

    @Override
    public void giveTo(Player player) {
        getDrop().forEach(item -> {
            PlayerUtil.givePlayerItemSafe(player, item);
        });
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        if (fancyDrop) {
            dropFancy(context, location);
        } else {
            getDrop().forEach(item -> {
                location.getWorld().dropItemNaturally(location, item);
            });
        }
    }

    /**
     * Drops the fancy item at the given location.
     *
     * @param context the drop context.
     * @param location the location to drop.
     * @return the fancy item drop.
     */
    public FancyItemDrop dropFancy(DropContext context, Location location) {
        FancyItemDrop drop = FancyItemDrop.spawn(location.clone().add(0, 1, 0), items.values().stream().map(ItemStackBuilder::buildFromTemplate).collect(Collectors.toList()));
        if (playerLocked && context.getPlayer() != null) {
            drop.getPickupPlayers().add(context.getPlayer().getUniqueId());
        }
        drop.setSpawnedBy(this);
        return drop;
    }

    @Override
    public boolean isPhysical() {
        return !giveDirectly;
    }
}
