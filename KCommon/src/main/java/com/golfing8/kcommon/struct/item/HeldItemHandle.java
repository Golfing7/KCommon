package com.golfing8.kcommon.struct.item;

import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.struct.Hand;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * An item handle for a player's held item in main or off-hand.
 */
@AllArgsConstructor
public class HeldItemHandle implements ItemHandle {
    private final Player player;
    private final Hand hand;

    @Override
    public ItemStack get() {
        return NMS.getTheNMS().getMagicItems().getItemInHand(player, hand);
    }

    @Override
    public void set(ItemStack item) {
        NMS.getTheNMS().getMagicItems().setItemInHand(player, hand, item);
    }
}
