package com.golfing8.kcommon.nms.v1_8.inventory;

import com.golfing8.kcommon.nms.ItemCapturePlayer;
import net.minecraft.server.v1_8_R3.PlayerInventory;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * NMS 1.8 item capture inventory
 */
public class ItemCaptureInventory extends CraftInventoryPlayer {
    private final ItemCapturePlayer player;

    public ItemCaptureInventory(PlayerInventory inventory, ItemCapturePlayer player) {
        super(inventory);

        this.player = player;
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(ItemStack... items) {
        for (ItemStack item : items) {
            player.getInventoryItemHooks().forEach(consumer -> consumer.accept(item));
        }
        return new HashMap<>();
    }

    @Override
    public void setItem(int index, ItemStack item) {
        player.getInventoryItemHooks().forEach(consumer -> consumer.accept(item));
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        player.getInventoryItemHooks().forEach(consumer -> consumer.accept(stack));
    }
}
