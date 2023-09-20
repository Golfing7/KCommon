package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

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
     * @param player the player to remove the effect from.
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
     * @param player the player to give the item to.
     * @param itemStack the item to give them.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack){
        givePlayerItemSafe(player, itemStack, false);
    }

    /**
     * Gives the player the given item or drops it at their feet.
     *
     * @param player the player to give the item to.
     * @param itemStack the item to give them.
     * @param silent if a message should be sent or not.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack, boolean silent){
        int total = itemStack.getAmount();

        for(int z = 0; z < total; z += 64){
            int toGiveThisTime = Math.min(64, total - z);

            itemStack.setAmount(toGiveThisTime);

            if(player.getInventory().firstEmpty() == -1){
                player.getWorld().dropItem(player.getLocation(), itemStack.clone());
                if(!silent)
                    player.sendMessage(MS.parseSingle("&cYour inventory was full. Check your feet!"));
            }else{
                player.getInventory().addItem(itemStack.clone());
            }
        }
    }
}
