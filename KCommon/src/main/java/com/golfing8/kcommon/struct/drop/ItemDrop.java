package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Drop for item stacks.
 */
@Getter
public class ItemDrop extends Drop<ItemStack> {
    private Map<String, ItemStackBuilder> items;
    public ItemDrop(double chance, String dropGroup, Map<String, ItemStackBuilder> items) {
        super(chance, dropGroup);
        this.items = items;
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
    public void dropAt(Location location) {
        getDrop().forEach(item -> {
            location.getWorld().dropItemNaturally(location, item);
        });
    }

    @Override
    public boolean isPhysical() {
        return true;
    }
}
