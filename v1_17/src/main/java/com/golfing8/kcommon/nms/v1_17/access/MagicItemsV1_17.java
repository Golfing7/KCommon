package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.v1_17.item.ItemStackV1_17;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class MagicItemsV1_17 implements NMSMagicItems {
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
    public void setCustomModelData(ItemMeta meta, int modelData) {
        meta.setCustomModelData(modelData);
    }

    @Override
    public NMSItemStack wrapItemStack(ItemStack itemStack) {
        return new ItemStackV1_17(CraftItemStack.asNMSCopy(itemStack));
    }
}
