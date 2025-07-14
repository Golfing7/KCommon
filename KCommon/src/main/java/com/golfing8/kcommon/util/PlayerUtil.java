package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;
import java.util.Collections;

/**
 * Stores utility classes pertaining to players.
 */
@UtilityClass
public final class PlayerUtil {

    /**
     * Removes the given potion effect IF it's the most 'dominant' on the player.
     * <p>
     * e.g. Player has Strength 2 for 30 seconds. If the passed in potion effect
     * is Strength 1, the potion effect will not be removed.
     * </p>
     *
     * @param player       the player to remove the effect from.
     * @param potionEffect the potion effect.
     * @return if the potion effect was removed.
     */
    public static boolean removePotionEffectNoOverride(Player player, PotionEffect potionEffect) {
        for (PotionEffect effect : player.getActivePotionEffects()) {
            if (effect.getType().equals(potionEffect.getType())) {
                if (effect.getAmplifier() > potionEffect.getAmplifier())
                    return false;
            }
        }

        player.removePotionEffect(potionEffect.getType());
        return true;
    }

    /**
     * Gives the player the given item or drops it at their feet.
     *
     * @param player    the player to give the item to.
     * @param itemStack the item to give them.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack) {
        givePlayerItemsSafe(player, Collections.singleton(itemStack), false);
    }

    /**
     * Gives the player the given item or drops it at their feet.
     *
     * @param player    the player to give the item to.
     * @param itemStack the item to give them.
     * @param silent    if a message should be sent or not.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack, boolean silent) {
        givePlayerItemsSafe(player, Collections.singleton(itemStack), silent);
    }

    /**
     * Gives the player the given items or drops them at their feet.
     *
     * @param player     the player to give the item to.
     * @param itemStacks the items to give them.
     */
    public static void givePlayerItemsSafe(Player player, Collection<ItemStack> itemStacks) {
        givePlayerItemsSafe(player, itemStacks, false);
    }

    /**
     * Gives the player the given items safely, dropping excess on the ground
     *
     * @param player     the player
     * @param itemStacks the item stacks
     * @param silent     if the overflow message should be sent
     */
    public static void givePlayerItemsSafe(Player player, Collection<ItemStack> itemStacks, boolean silent) {
        boolean notify = false;
        for (ItemStack itemStack : itemStacks) {
            int total = itemStack.getAmount();

            for (int z = 0; z < total; z += 64) {
                int toGiveThisTime = Math.min(64, total - z);

                itemStack.setAmount(toGiveThisTime);

                if (player.getInventory().firstEmpty() == -1) {
                    player.getWorld().dropItem(player.getLocation(), itemStack.clone());
                    if (!silent)
                        notify = true;
                } else {
                    player.getInventory().addItem(itemStack.clone());
                }
            }
        }
        if (notify)
            player.sendMessage(MS.parseSingle("&cYour inventory was full. Check your feet!"));
    }
}
