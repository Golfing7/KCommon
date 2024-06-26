package com.golfing8.kcommon.struct.item;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.event.FancyItemPickupEvent;
import com.golfing8.kcommon.hook.holograms.Hologram;
import com.golfing8.kcommon.hook.holograms.HologramProvider;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Represents an item drop that is displayed on an armor stand rather than the ground.
 * <p>
 * These fancy item drops are NOT persistent.
 * </p>
 */
@Getter
public class FancyItemDrop extends BukkitRunnable {
    private final Location location;
    private final Collection<ItemStack> items;
    private final ItemStack icon;
    private final Hologram displayHologram;
    /** The players that can pick this item up. If empty, everyone can pick it up. */
    private final Set<UUID> pickupPlayers;
    @Setter
    private double pickupRange = 1.5D;
    @Setter
    private long expiryTicks = 20L * 60L * 50L;
    @Setter
    private long pickupDelayTicks = 20L;
    private boolean valid;

    private FancyItemDrop(Location location, Collection<ItemStack> items, ItemStack icon) {
        if (items.isEmpty())
            throw new IllegalArgumentException("Items cannot be empty!");

        this.location = location;
        this.items = Lists.newArrayList(items);
        this.icon = icon;
        this.pickupPlayers = new HashSet<>();

        this.displayHologram = HologramProvider.getInstance().createHologram(location);
        if (icon.hasItemMeta() && icon.getItemMeta().hasDisplayName()) {
            this.displayHologram.addLine(icon.getItemMeta().getDisplayName());
        }
        this.displayHologram.addLine(icon);

        this.valid = true;

        this.runTaskTimer(KCommon.getInstance(), 0L, 1L);
    }

    /**
     * Deletes this fancy item drop.
     */
    public void remove() {
        cancel();
    }

    @Override
    public synchronized void cancel() throws IllegalStateException {
        super.cancel();

        if (!this.valid)
            return;

        this.displayHologram.delete();
        this.valid = false;
    }

    @Override
    public void run() {
        if (this.expiryTicks-- < 0L || this.items.isEmpty()) {
            remove();
            return;
        }

        // While there's a pickup delay, wait.
        if (this.pickupDelayTicks-- > 0)
            return;

        for (Entity entity : this.location.getNearbyEntities(pickupRange, pickupRange, pickupRange)) {
            if (!(entity instanceof Player))
                continue;

            Player player = (Player) entity;
            if (!pickupPlayers.isEmpty() && !pickupPlayers.contains(player.getUniqueId()))
                continue;

            this.items.removeIf(item -> {
                FancyItemPickupEvent fancyPickupEvent = new FancyItemPickupEvent(player, this, item);
                Bukkit.getPluginManager().callEvent(fancyPickupEvent);
                if (fancyPickupEvent.isCancelled())
                    return false;

                var leftOver = player.getInventory().addItem(item);
                if (leftOver.isEmpty())
                    return true;

                item.setAmount(leftOver.get(0).getAmount());
                return false;
            });
        }
    }

    public static FancyItemDrop spawn(Location location, Collection<ItemStack> items, ItemStack icon) {
        return new FancyItemDrop(location, items, icon);
    }
}
