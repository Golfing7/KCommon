package com.golfing8.kcommon.util;

import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Stores utility classes pertaining to players.
 */
@UtilityClass
public final class PlayerUtil {

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
