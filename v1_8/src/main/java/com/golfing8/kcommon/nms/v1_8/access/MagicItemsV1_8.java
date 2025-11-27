package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.Hand;
import com.golfing8.kcommon.nms.struct.PotionData;
import com.golfing8.kcommon.nms.v1_8.item.ItemStackV1_8;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import lombok.var;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.server.v1_8_R3.AttributeModifier;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class MagicItemsV1_8 implements NMSMagicItems {
    @Override
    public EntityType getSpawnerType(ItemStack stack) {
        if (stack == null) return EntityType.PIG;

        if (stack.getType() != Material.MOB_SPAWNER) return EntityType.PIG;

        if (!stack.hasItemMeta()) return EntityType.PIG;

        NBTItem nbtItem = new NBTItem(stack);

        NBTCompound nbtCompound = nbtItem.getCompound("BlockEntityTag");

        if (nbtCompound == null) {
            if (nbtItem.hasKey("type")) {
                return translateNMSTypes(nbtItem.getString("type").toUpperCase());
            }
            return EntityType.PIG;
        }

        if (nbtCompound.getString("EntityId") == null) return EntityType.PIG;

        return translateNMSTypes(nbtCompound.getString("EntityId").toUpperCase());
    }

    @Override
    public void applyName(ItemMeta meta, @Nullable String name) {
        if (name == null) {
            meta.setDisplayName(null);
        } else {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(ComponentUtils.toComponent(name)));
        }
    }

    @Override
    public void applyLore(ItemMeta meta, List<String> lore) {
        meta.setLore(ComponentUtils.toComponent(lore).stream().map(LegacyComponentSerializer.legacySection()::serialize).collect(Collectors.toList()));
    }

    @Override
    public void applyComponentName(ItemMeta meta, @Nullable Component component) {
        if (component == null) {
            meta.setDisplayName(null);
        } else {
            meta.setDisplayName(LegacyComponentSerializer.legacySection().serialize(component));
        }
    }

    @Override
    public void applyComponentLore(ItemMeta meta, @Nullable List<? extends Component> components) {
        if (components == null) {
            meta.setLore(null);
        } else {
            meta.setLore(components.stream().map(LegacyComponentSerializer.legacySection()::serialize).collect(Collectors.toList()));
        }
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
    public @Nullable Component getComponentDisplayName(ItemMeta meta) {
        if (meta.hasDisplayName()) {
            return LegacyComponentSerializer.legacySection().deserialize(meta.getDisplayName());
        } else {
            return null;
        }
    }

    @Override
    public @Nullable List<Component> getComponentLore(ItemMeta meta) {
        if (meta.hasLore()) {
            return meta.getLore().stream().map(LegacyComponentSerializer.legacySection()::deserialize).collect(Collectors.toList());
        } else {
            return null;
        }
    }

    @Override
    public String getDisplayName(ItemStack itemStack) {
        if (itemStack == null)
            return null;

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();
            if (meta.hasDisplayName())
                return meta.getDisplayName();
        }
        return new ItemStackV1_8(CraftItemStack.asNMSCopy(itemStack)).getI18DisplayName();
    }

    @Override
    public int getRemainingItemDurability(ItemStack stack) {
        return stack.getType().getMaxDurability() > 0 ? stack.getType().getMaxDurability() - stack.getDurability() : -1;
    }

    @Override
    public boolean setRemainingItemDurability(ItemStack stack, int amount) {
        int remaining = getRemainingItemDurability(stack);

        if (remaining == -1)
            return false;

        stack.setDurability((short) (stack.getType().getMaxDurability() - amount));
        return stack.getDurability() > stack.getType().getMaxDurability();
    }

    @Override
    public boolean isUnbreakable(ItemMeta meta) {
        return meta.spigot().isUnbreakable();
    }

    @Override
    public void setUnbreakable(ItemMeta meta, boolean value) {
        meta.spigot().setUnbreakable(value);
    }

    @Override
    public void setAttributeModifiers(ItemStack stack, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers) {
        NBT.modify(stack, (nbt) -> {
            // First clear the list
            if (modifiers == null) {
                nbt.removeKey("AttributeModifiers");
                return;
            }

            nbt.getCompoundList("AttributeModifiers").clear();

            for (var entry : modifiers.entrySet()) {
                for (var modifier : entry.getValue()) {
                    ReadWriteNBTCompoundList list = nbt.getCompoundList("AttributeModifiers");
                    ReadWriteNBT newCompound = list.addCompound();
                    newCompound.setLong("UUIDMost", modifier.getUuid().getMostSignificantBits());
                    newCompound.setLong("UUIDLeast", modifier.getUuid().getLeastSignificantBits());
                    newCompound.setString("Name", modifier.getName());
                    newCompound.setDouble("Amount", modifier.getAmount());
                    newCompound.setInteger("Operation", modifier.getOperation().ordinal());

                    String name = null;
                    switch (entry.getKey()) {
                        case GENERIC_MAX_HEALTH:
                            name = "generic.maxHealth";
                            break;
                        case GENERIC_FOLLOW_RANGE:
                            name = "generic.followRange";
                            break;
                        case GENERIC_KNOCKBACK_RESISTANCE:
                            name = "generic.knockbackResistance";
                            break;
                        case GENERIC_MOVEMENT_SPEED:
                            name = "generic.movementSpeed";
                            break;
                        case GENERIC_ATTACK_DAMAGE:
                            name = "generic.attackDamage";
                            break;
                    }
                    // If the name is still null, just continue.
                    if (name == null)
                        continue;
                    newCompound.setString("AttributeName", name);
                }
            }
        });
    }

    @Override
    public void setExtraAttributeModifiers(ItemStack stack, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers) {
        Multimap<String, AttributeModifier> defaultAttributes = CraftItemStack.asNMSCopy(stack).getItem().i();
        // Is the player just wanting to clear 'extra' modifiers?
        if (defaultAttributes.isEmpty() && modifiers == null) {
            setAttributeModifiers(stack, null);
            return;
        }
        Map<EntityAttribute, Set<EntityAttributeModifier>> newModifiers = modifiers == null ? new HashMap<>() : new HashMap<>(modifiers);
        for (Map.Entry<String, AttributeModifier> attributeEntry : defaultAttributes.entries()) {
            newModifiers.computeIfAbsent(EntityAttribute.byName(attributeEntry.getKey()), (k) -> new HashSet<>())
                    .add(new EntityAttributeModifier(attributeEntry.getValue().a(), attributeEntry.getValue().b(), attributeEntry.getValue().d(), EntityAttributeModifier.Operation.values()[attributeEntry.getValue().c()]));
        }
        setAttributeModifiers(stack, newModifiers);
    }

    private Class<?> craftMetaSkullClass;
    private FieldHandle<GameProfile> gameProfileFieldHandle;

    @SuppressWarnings("unchecked")
    private void setupMetaSkullAccess() {
        if (craftMetaSkullClass != null)
            return;

        try {
            craftMetaSkullClass = Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaSkull");
            gameProfileFieldHandle = (FieldHandle<GameProfile>) FieldHandles.getHandle("profile", craftMetaSkullClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find CraftMetaSkull class", e);
        }
    }

    @Override
    public void setSkullOwningPlayer(SkullMeta meta, OfflinePlayer offlinePlayer) {
        setupMetaSkullAccess();

        gameProfileFieldHandle.set(meta, new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
    }

    @Override
    public void setSkullTexture(SkullMeta meta, String base64Texture) {
        setupMetaSkullAccess();

        gameProfileFieldHandle.set(meta, NMSMagicItems.makeProfile(base64Texture));
    }

    @Override
    public void setItemModel(ItemMeta meta, @Nullable String key) {
        throw new UnsupportedOperationException("1.8 does not support item models.");
    }

    @Override
    public void setBaseEffect(PotionMeta meta, PotionData data) {
        throw new UnsupportedOperationException("1.8 does not support potion data types."); // TODO Maybe try to find a way?
    }

    @Override
    public PotionData getBaseEffect(PotionMeta meta) {
        throw new UnsupportedOperationException("1.8 does not support potion data types."); // TODO Maybe try to find a way?
    }

    @Override
    public void setCustomModelData(ItemMeta meta, int modelData) {
        throw new UnsupportedOperationException("1.8 does not support custom model data.");
    }

    @Override
    public ItemStack getItemInHand(Player player, Hand hand) {
        if (hand == Hand.MAIN) {
            return player.getItemInHand();
        }
        return null;
    }

    @Override
    public void setItemInHand(Player player, Hand hand, ItemStack stack) {
        if (hand == Hand.OFF_HAND)
            throw new UnsupportedOperationException("Cannot set off hand on 1.8");

        player.setItemInHand(stack);
    }

    @Override
    public NMSItemStack wrapItemStack(ItemStack itemStack) {
        return new ItemStackV1_8(CraftItemStack.asNMSCopy(itemStack));
    }

    @Override
    public void setUnstackable(ItemStack itemStack, boolean value) {
        NBT.modify(itemStack, (nbt) -> {
            if (value) {
                nbt.setString("unstackable", UUID.randomUUID().toString());
            } else {
                nbt.removeKey("unstackable");
            }
        });
    }

    public static EntityType translateNMSTypes(String nmsType) {
        switch (nmsType.toLowerCase()) {
            case "pigzombie":
                return EntityType.PIG_ZOMBIE;
            case "complexpart":
                return EntityType.COMPLEX_PART;
            case "cavespider":
                return EntityType.CAVE_SPIDER;
            case "irongolem":
            case "villagergolem":
                return EntityType.IRON_GOLEM;
            case "magmacube":
            case "lavaslime":
                return EntityType.MAGMA_CUBE;
            case "mushroomcow":
                return EntityType.MUSHROOM_COW;
            //I have ZERO idea why SilkSpawners thinks "ozelot" is acceptable
            case "ozelot":
                return EntityType.OCELOT;
            case "entityhorse":
                return EntityType.HORSE;
            default:
                return EntityType.valueOf(nmsType.toUpperCase());
        }
    }
}
