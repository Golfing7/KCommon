package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.PotionData;
import com.golfing8.kcommon.nms.v1_8.item.ItemStackV1_8;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBTCompoundList;
import lombok.var;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MagicItemsV1_8 implements NMSMagicItems {
    @Override
    public EntityType getSpawnerType(ItemStack stack) {
        if (stack == null) return EntityType.PIG;

        if (stack.getType() != Material.MOB_SPAWNER) return EntityType.PIG;

        if (!stack.hasItemMeta()) return EntityType.PIG;

        NBTItem nbtItem = new NBTItem(stack);

        NBTCompound nbtCompound = nbtItem.getCompound("BlockEntityTag");

        if(nbtCompound == null){
            if(nbtItem.hasKey("type")){
                return translateNMSTypes(nbtItem.getString("type").toUpperCase());
            }
            return EntityType.PIG;
        }

        if(nbtCompound.getString("EntityId") == null)return EntityType.PIG;

        return translateNMSTypes(nbtCompound.getString("EntityId").toUpperCase());
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

        if(remaining == -1)
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
    public NMSItemStack wrapItemStack(ItemStack itemStack) {
        return new ItemStackV1_8(CraftItemStack.asNMSCopy(itemStack));
    }

    public static EntityType translateNMSTypes(String nmsType){
        switch(nmsType.toLowerCase()){
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
