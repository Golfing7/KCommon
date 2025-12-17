package com.golfing8.kcommon.struct.drop;

import com.cryptomorin.xseries.XEnchantment;
import com.golfing8.kcommon.struct.item.FancyItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.MathExpressions;
import com.golfing8.kcommon.util.MathUtil;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Drop for item stacks.
 */
@Getter
public class ItemDrop extends Drop<ItemStack> {
    private final Map<String, ItemStackBuilder> items;
    private final boolean giveDirectly;
    private final boolean fancyDrop;
    private final boolean playerLocked;
    private final boolean boostQuantity;
    private final boolean lootingEnabled;
    private final boolean fortuneEnabled;
    private final String lootingFormula;

    public ItemDrop(double chance,
                    @Nullable String displayName,
                    double maxBoost,
                    Map<String, ItemStackBuilder> items,
                    boolean giveDirectly,
                    boolean fancyDrop,
                    boolean playerLocked,
                    boolean boostQuantity,
                    boolean lootingEnabled,
                    boolean fortuneEnabled,
                    @Nullable String lootingFormula) {
        super(chance, displayName, maxBoost);
        this.items = items;
        this.giveDirectly = giveDirectly;
        this.fancyDrop = fancyDrop;
        this.playerLocked = playerLocked;
        this.boostQuantity = boostQuantity;
        this.lootingEnabled = lootingEnabled;
        this.fortuneEnabled = fortuneEnabled;
        this.lootingFormula = lootingFormula == null ? "rand1({LOOTING})" : lootingFormula;
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
        return items.values().stream().map(builder -> {
            ItemStack itemStack = builder.buildFromTemplate();
            if (effectiveBoost != 1.0D && boostQuantity) {
                itemStack.setAmount(MathUtil.roundRandomly(itemStack.getAmount() * effectiveBoost));
            }
            return itemStack;
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
        if (context.getPlayer() == null || !lootingEnabled && !fortuneEnabled)
            return getDrop(context.getBoost());

        ItemStack inHand = context.getPlayer().getItemInHand();
        if (inHand == null || !inHand.hasItemMeta())
            return getDrop(context.getBoost());

        int lootingLevel;
        int fortuneLevel;
        List<ItemStack> itemStacks = new ArrayList<>();
        if (lootingEnabled && (lootingLevel = inHand.getEnchantmentLevel(XEnchantment.LOOTING.get())) > 0) {
            int extraDrops = (int) MathExpressions.evaluate(lootingFormula, "LOOTING", lootingLevel);
            itemStacks.addAll(getDrop().stream().peek(item -> item.setAmount(item.getAmount() + extraDrops)).collect(Collectors.toList()));
        } else if (fortuneEnabled && (fortuneLevel = inHand.getEnchantmentLevel(XEnchantment.LOOTING.get())) > 0) {
            int extraDrops = (int) MathExpressions.evaluate(lootingFormula, "LOOTING", fortuneLevel);
            itemStacks.addAll(getDrop().stream().peek(item -> item.setAmount(item.getAmount() + extraDrops)).collect(Collectors.toList()));
        } else {
            itemStacks.addAll(getDrop());
        }

        if (boostQuantity) {
            itemStacks.forEach(item -> item.setAmount(MathUtil.roundRandomly(item.getAmount() * context.getBoost())));
        }
        return itemStacks;
    }

    @Override
    public void giveTo(DropContext context) {
        getDrop(context).forEach(item -> {
            PlayerUtil.givePlayerItemSafe(context.getPlayer(), item);
        });
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        if (fancyDrop) {
            dropFancy(context, location);
        } else {
            getDrop(context).forEach(item -> {
                location.getWorld().dropItemNaturally(location, item);
            });
        }
    }

    /**
     * Drops the fancy item at the given location.
     *
     * @param context  the drop context.
     * @param location the location to drop.
     * @return the fancy item drop.
     */
    public FancyItemDrop dropFancy(DropContext context, Location location) {
        FancyItemDrop drop = FancyItemDrop.spawn(location.clone().add(0, 1, 0), getDrop(context));
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
