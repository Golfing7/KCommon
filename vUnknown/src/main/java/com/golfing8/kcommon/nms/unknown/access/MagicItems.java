package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.unknown.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MagicItems implements NMSMagicItems {
    @Override
    public EntityType getSpawnerType(ItemStack stack) {
        if (stack == null) return EntityType.PIG;

        if (stack.getType() != Material.SPAWNER) return EntityType.PIG;

        if (!stack.hasItemMeta()) return EntityType.PIG;

        BlockStateMeta meta = (BlockStateMeta) stack.getItemMeta();

        BlockState state = meta.getBlockState();

        return ((CreatureSpawner) state).getSpawnedType();
    }

    @Override
    public int getRemainingItemDurability(ItemStack stack) {
        if(!(stack.getItemMeta() instanceof Damageable))
            return -1;

        Damageable damageable = (Damageable) stack.getItemMeta();

        return stack.getType().getMaxDurability() - damageable.getDamage();
    }

    @Override
    public String getDisplayName(ItemStack itemStack) {
        if (itemStack == null)
            return null;

        Component display = itemStack.displayName();
        if (display instanceof TranslatableComponent translatable && translatable.args().size() == 1) {
            return LegacyComponentSerializer.legacyAmpersand().serialize(translatable.args().get(0));
        } else {
            return LegacyComponentSerializer.legacyAmpersand().serialize(display);
        }
    }

    @Override
    public boolean setRemainingItemDurability(ItemStack stack, int amount) {
        int remaining = getRemainingItemDurability(stack);

        if(remaining == -1)
            return false;

        Damageable damageable = (Damageable) stack.getItemMeta();
        damageable.setDamage(stack.getType().getMaxDurability() - amount);
        return damageable.getDamage() > stack.getType().getMaxDurability();
    }

    @Override
    public void setUnbreakable(ItemMeta meta, boolean value) {
        meta.setUnbreakable(value);
    }

    @Override
    public void applyName(ItemMeta meta, @Nullable String name) {
        meta.displayName(ComponentUtils.toComponent(name));
    }

    @Override
    public void applyLore(ItemMeta meta, List<String> lore) {
        if (lore == null) {
            meta.lore(null);
            return;
        }
        meta.lore(ComponentUtils.toComponent(lore));
    }

    @Override
    public String getMMDisplayName(ItemMeta meta) {
        Component display = meta.displayName();
        return display == null ? null : MiniMessage.miniMessage().serialize(display);
    }

    @Override
    public List<String> getMMLore(ItemMeta meta) {
        List<Component> lore = meta.lore();
        if (lore == null)
            return null;

        return lore.stream().map(MiniMessage.miniMessage()::serialize).toList();
    }

    @Override
    public void setCustomModelData(ItemMeta meta, int modelData) {
        meta.setCustomModelData(modelData);
    }

    @Override
    public NMSItemStack wrapItemStack(ItemStack itemStack) {
        return new com.golfing8.kcommon.nms.unknown.item.ItemStack(itemStack);
    }
}
