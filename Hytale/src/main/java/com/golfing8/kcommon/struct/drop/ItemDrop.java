package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.util.MathUtil;
import com.golfing8.kcommon.util.HytalePlayers;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Represents a Drop for item stacks.
 */
@Getter
public class ItemDrop extends Drop<ItemStack> {
    private Map<String, ItemStack> items;
    private boolean giveDirectly;
    private boolean boostQuantity;

    private ItemDrop() {
        // Private for codec use
    }

    public ItemDrop(double chance,
                    @Nullable String displayName,
                    double maxBoost,
                    Map<String, ItemStack> items,
                    boolean giveDirectly,
                    boolean boostQuantity) {
        super(chance, displayName, maxBoost);
        this.items = items;
        this.giveDirectly = giveDirectly;
        this.boostQuantity = boostQuantity;
    }

    @Override
    public List<ItemStack> getDrop() {
        return getDrop(1.0D);
    }

    /**
     * Gets a list of item drops with the given boost applied
     *
     * @param boost the boost to apply
     * @return the drops
     */
    public List<ItemStack> getDrop(double boost) {
        double effectiveBoost = Math.min(boost, getMaxBoost());
        return items.values().stream().map(template -> {
            ItemStack item = template;
            if (effectiveBoost != 1.0D && boostQuantity) {
                item = item.withQuantity(MathUtil.roundRandomly(item.getQuantity() * effectiveBoost));
            }
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * Gets the drops generated for the given player.
     *
     * @param player the player.
     * @return the generated drops.
     */
    public List<ItemStack> getDrop(@Nullable Player player) {
        return getDrop(new DropContext(player));
    }

    /**
     * Gets the drops based upon the given context
     *
     * @param context the context
     * @return the drops
     */
    public List<ItemStack> getDrop(DropContext context) {
        if (boostQuantity) {
            double boost = Math.min(getMaxBoost(), context.getBoost());
            // withQuantity(0) returns null, so drop stacks that rounded down to nothing
            return getDrop().stream().map(item -> item.withQuantity(MathUtil.roundRandomly(item.getQuantity() * boost))).filter(Objects::nonNull).toList();
        } else {
            return getDrop(context.getBoost());
        }
    }

    @Override
    public void giveTo(DropContext context) {
        HytalePlayers.givePlayerItemsSafe(context.getPlayer(), getDrop(context));
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        World world = Universe.get().getWorld(location.getWorld());
        if (world != null) {
            Holder<EntityStore>[] items = ItemComponent.generateItemDrops(world.getEntityStore().getStore(), getDrop(context), location.getPosition(), location.getRotation());
            world.getEntityStore().getStore().addEntities(items, AddReason.SPAWN);
        }
    }

    @Override
    public boolean isPhysical() {
        return !giveDirectly;
    }
}
