package com.golfing8.kcommon.struct.drop;

import com.cryptomorin.xseries.XEnchantment;
import com.golfing8.kcommon.struct.item.FancyItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.MathExpressions;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import net.objecthunter.exp4j.ExpressionBuilder;
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
    private boolean lootingEnabled;
    private boolean fortuneEnabled;
    private String lootingFormula;
    public ItemDrop(double chance,
                    @Nullable String displayName,
                    Map<String, ItemStackBuilder> items,
                    boolean giveDirectly,
                    boolean fancyDrop,
                    boolean playerLocked,
                    boolean lootingEnabled,
                    boolean fortuneEnabled,
                    @Nullable String lootingFormula) {
        super(chance, displayName);
        this.items = items;
        this.giveDirectly = giveDirectly;
        this.fancyDrop = fancyDrop;
        this.playerLocked = playerLocked;
        this.lootingEnabled = lootingEnabled;
        this.fortuneEnabled = fortuneEnabled;
        this.lootingFormula = lootingFormula == null ? "rand1({LOOTING})" : lootingFormula;
    }

    @Override
    public List<ItemStack> getDrop() {
        return items.values().stream().map(ItemStackBuilder::buildFromTemplate).collect(Collectors.toList());
    }

    /**
     * Gets the drops generated for the given player.
     *
     * @param player the player.
     * @return the generated drops.
     */
    public List<ItemStack> getDrop(@Nullable Player player) {
        if (player == null || (!lootingEnabled && !fortuneEnabled))
            return getDrop();

        ItemStack inHand = player.getItemInHand();
        if (inHand == null || !inHand.hasItemMeta())
            return getDrop();

        int lootingLevel;
        int fortuneLevel;
        if (lootingEnabled && (lootingLevel = inHand.getEnchantmentLevel(XEnchantment.LOOT_BONUS_MOBS.getEnchant())) > 0) {
            int extraDrops = (int) MathExpressions.evaluate(lootingFormula, "LOOTING", lootingLevel);
            return getDrop().stream().peek(item -> item.setAmount(item.getAmount() + extraDrops)).collect(Collectors.toList());
        }
        if (fortuneEnabled && (fortuneLevel = inHand.getEnchantmentLevel(XEnchantment.LOOT_BONUS_BLOCKS.getEnchant())) > 0) {
            int extraDrops = (int) MathExpressions.evaluate(lootingFormula, "LOOTING", fortuneLevel);
            return getDrop().stream().peek(item -> item.setAmount(item.getAmount() + extraDrops)).collect(Collectors.toList());
        }

        return getDrop();
    }

    @Override
    public void giveTo(Player player) {
        getDrop(player).forEach(item -> {
            PlayerUtil.givePlayerItemSafe(player, item);
        });
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        if (fancyDrop) {
            dropFancy(context, location);
        } else {
            getDrop(context.getPlayer()).forEach(item -> {
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
        FancyItemDrop drop = FancyItemDrop.spawn(location.clone().add(0, 1, 0), getDrop(context.getPlayer()));
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
