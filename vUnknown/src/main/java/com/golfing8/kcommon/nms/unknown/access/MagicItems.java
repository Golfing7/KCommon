package com.golfing8.kcommon.nms.unknown.access;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
    public boolean isUnbreakable(ItemMeta meta) {
        return meta.isUnbreakable();
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
    public void setExtraAttributeModifiers(ItemStack stack, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers) {
        ItemMeta unmodifiedMeta = new ItemStack(stack.getType()).getItemMeta();
        var originalAttributes = unmodifiedMeta.getAttributeModifiers();
        Multimap<Attribute, AttributeModifier> defaultAttributes = originalAttributes == null ? HashMultimap.create() : HashMultimap.create(originalAttributes);
        if (modifiers != null) {
            for (var entry : modifiers.entrySet()) {
                Attribute attribute = Attribute.valueOf(entry.getKey().name());
                for (EntityAttributeModifier modifier : entry.getValue()) {
                    defaultAttributes.put(attribute, new AttributeModifier(
                            modifier.getUuid(),
                            modifier.getName(),
                            modifier.getAmount(),
                            AttributeModifier.Operation.valueOf(modifier.getOperation().name()),
                            modifier.getSlot())
                    );
                }
            }
        }
        ItemMeta meta = stack.getItemMeta();
        meta.setAttributeModifiers(defaultAttributes);
        stack.setItemMeta(meta);
    }

    @Override
    public void setSkullOwningPlayer(SkullMeta meta, OfflinePlayer offlinePlayer) {
        meta.setOwningPlayer(offlinePlayer);
    }

    @Override
    public void setSkullTexture(SkullMeta meta, String base64Texture) {
        GameProfile mojProfile = NMSMagicItems.makeProfile(base64Texture);
        PlayerProfile bukkitProfile = Bukkit.createProfile(mojProfile.getId(), mojProfile.getName());
        bukkitProfile.setProperty(new ProfileProperty("textures", base64Texture));
        meta.setPlayerProfile(bukkitProfile);
    }

    @Override
    public void setItemModel(ItemMeta meta, @Nullable String key) {
        if (key == null) {
            meta.setItemModel(null);
        } else {
            meta.setItemModel(NamespacedKey.fromString(key));
        }
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

    @Override
    public void setUnstackable(ItemStack itemStack, boolean value) {
        itemStack.getItemMeta().setMaxStackSize(value ? 1 : null);
    }
}
