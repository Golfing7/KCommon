package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.v1_17.item.ItemStackV1_17;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public boolean isUnbreakable(ItemMeta meta) {
        return meta.isUnbreakable();
    }

    @Override
    public void applyName(ItemMeta meta, @Nullable String name) {
        meta.setDisplayName(name);
    }

    @Override
    public void applyLore(ItemMeta meta, List<String> lore) {
        meta.setLore(lore);
    }

    @Override
    public String getMMDisplayName(ItemMeta meta) {
        return meta.getDisplayName();
    }

    @Override
    public List<String> getMMLore(ItemMeta meta) {
        return meta.getLore();
    }

    @Override
    public void setUnbreakable(ItemMeta meta, boolean value) {
        meta.setUnbreakable(value);
    }

    @Override
    public void setAttributeModifiers(ItemStack stack, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers) {
        ItemMeta meta = stack.getItemMeta();
        if (modifiers != null) {
            Multimap<Attribute, AttributeModifier> tlModifiers = HashMultimap.create();
            for (var entry : modifiers.entrySet()) {
                Attribute attribute = Attribute.valueOf(entry.getKey().name());
                for (EntityAttributeModifier modifier : entry.getValue()) {
                    tlModifiers.put(attribute, new AttributeModifier(
                            modifier.getUuid(),
                            modifier.getName(),
                            modifier.getAmount(),
                            AttributeModifier.Operation.valueOf(modifier.getOperation().name()),
                            modifier.getSlot())
                    );
                }
            }
            meta.setAttributeModifiers(tlModifiers);
        } else {
            meta.setAttributeModifiers(null);
        }
        stack.setItemMeta(meta);
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
