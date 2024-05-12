package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.PlayerUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

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
    private boolean giveDirectly;
    public ItemDrop(double chance, @Nullable String displayName, Map<String, ItemStackBuilder> items, boolean giveDirectly) {
        super(chance, displayName);
        this.items = items;
        this.giveDirectly = giveDirectly;
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
        return !giveDirectly;
    }
}
