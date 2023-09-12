package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicItems;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.v1_8.item.ItemStackV1_8;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
    public void setUnbreakable(ItemMeta meta, boolean value) {
        meta.spigot().setUnbreakable(value);
    }

    @Override
    public void setCustomModelData(ItemMeta meta, int modelData) {

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
