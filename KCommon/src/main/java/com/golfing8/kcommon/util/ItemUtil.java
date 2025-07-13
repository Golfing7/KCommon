package com.golfing8.kcommon.util;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.item.NMSItem;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import lombok.experimental.UtilityClass;
import lombok.var;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Utilities for {@link ItemStack} objects.
 */
@UtilityClass
public final class ItemUtil {

    /**
     * Gets the display name of the itemstack.
     *
     * @param itemStack the itemstack.
     * @return its displayed name
     */
    public static String getDisplayName(ItemStack itemStack) {
        if (itemStack == null)
            return "Air";

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getItemMeta().getDisplayName();
        } else {
            NMSItemStack stack = NMS.getTheNMS().getMagicItems().wrapItemStack(itemStack);
            return stack.getI18DisplayName();
        }
    }

    /**
     * Applies the placeholders to the given item.
     *
     * @param itemStack the item
     * @param placeholders the placeholders
     */
    public static void applyPlaceholders(ItemStack itemStack, Collection<Placeholder> placeholders) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        ItemMeta meta = itemStack.getItemMeta();
        if (meta.hasDisplayName()) {
            String displayName = NMS.getTheNMS().getMagicItems().getMMDisplayName(meta);
            NMS.getTheNMS().getMagicItems().applyName(meta, MS.parseSingle(displayName, placeholders));
        }

        if (!meta.hasLore()) {
            itemStack.setItemMeta(meta);
            return;
        }

        List<String> lore = NMS.getTheNMS().getMagicItems().getMMLore(meta);
        NMS.getTheNMS().getMagicItems().applyLore(meta, MS.parseAll(lore, placeholders));
        itemStack.setItemMeta(meta);
    }

    /**
     * Applies the multi-line placeholders to the given item.
     *
     * @param itemStack the item.
     * @param placeholders the placeholders.
     */
    public static void applyMPlaceholders(ItemStack itemStack, Collection<MultiLinePlaceholder> placeholders) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        ItemMeta meta = itemStack.getItemMeta();
        if (!meta.hasLore()) {
            return;
        }

        List<String> lore = NMS.getTheNMS().getMagicItems().getMMLore(meta);
        NMS.getTheNMS().getMagicItems().applyLore(meta, MS.parseAll(lore, placeholders));
        itemStack.setItemMeta(meta);
    }

    /**
     * Checks if the item is air or null or has an amount of 0.
     *
     * @param check the check.
     * @return true if the item is air or null.
     */
    public static boolean isAirOrNull(ItemStack check){
        return check == null || check.getType() == XMaterial.AIR.parseMaterial() || check.getAmount() <= 0;
    }

    public static void removeInNBT(ReadWriteNBT nbt, Map<String, Object> data) {
        for (var entry : data.entrySet()) {
            nbt.removeKey(entry.getKey());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void removeValueInNBT(ReadWriteNBT nbt, String key, Object value) {
        if (value instanceof Integer && nbt.hasTag(key, NBTType.NBTTagInt)) {
            int nbtValue = (Integer) value;
            if (nbtValue == nbt.getInteger(key))
                nbt.removeKey(key);
        } else if (value instanceof Float && nbt.hasTag(key, NBTType.NBTTagFloat)) {
            float nbtValue = (Float) value;
            if (nbtValue == nbt.getFloat(key))
                nbt.removeKey(key);
        } else if (value instanceof Double && nbt.hasTag(key, NBTType.NBTTagDouble)) {
            double nbtValue = (Double) value;
            if (nbtValue == nbt.getDouble(key))
                nbt.removeKey(key);
        } else if (value instanceof Byte && nbt.hasTag(key, NBTType.NBTTagByte)) {
            byte nbtValue = (Byte) value;
            if (nbtValue == nbt.getByte(key))
                nbt.removeKey(key);
        } else if (value instanceof Short && nbt.hasTag(key, NBTType.NBTTagShort)) {
            short nbtValue = (Short) value;
            if (nbtValue == nbt.getShort(key))
                nbt.removeKey(key);
        } else if (value instanceof Long && nbt.hasTag(key, NBTType.NBTTagLong)) {
            long nbtValue = (Long) value;
            if (nbtValue == nbt.getLong(key))
                nbt.removeKey(key);
        } else if (value instanceof String && nbt.hasTag(key, NBTType.NBTTagString)) {
            String nbtValue = (String) value;
            if (nbtValue.equals(nbt.getString(key)))
                nbt.removeKey(key);
        } else if (value instanceof Map && nbt.hasTag(key, NBTType.NBTTagCompound)) {
            var subCompound = nbt.getCompound(key);
            ((Map) value).forEach((k, v) -> {
                removeValueInNBT(subCompound, k.toString(), v);
            });
            if (subCompound.getKeys().isEmpty())
                nbt.removeKey(key);
        } else if (value instanceof Boolean && nbt.hasTag(key, NBTType.NBTTagByte)) {
            boolean nbtValue = (Boolean) value;
            if (nbtValue == nbt.getBoolean(key))
                nbt.removeKey(key);
        }
    }

    public static void setInNBT(ReadWriteNBT nbt, Map<String, Object> data) {
        for (var entry : data.entrySet()) {
            setValueInNBT(nbt, entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueInNBT(ReadWriteNBT nbtItem, String key, Object value) {
        if (value instanceof Integer) {
            nbtItem.setInteger(key, (Integer) value);
        } else if (value instanceof Float) {
            nbtItem.setFloat(key, (Float) value);
        } else if (value instanceof Double) {
            nbtItem.setDouble(key, (Double) value);
        } else if (value instanceof Byte) {
            nbtItem.setByte(key, (Byte) value);
        } else if (value instanceof Short) {
            nbtItem.setShort(key, (Short) value);
        } else if (value instanceof Long) {
            nbtItem.setLong(key, (Long) value);
        } else if (value instanceof String) {
            nbtItem.setString(key, (String) value);
        } else if (value instanceof Map) {
            var subCompound = nbtItem.getOrCreateCompound(key);
            ((Map) value).forEach((k, v) -> {
                setValueInNBT(subCompound, k.toString(), v);
            });
        } else if (value instanceof Boolean) {
            nbtItem.setBoolean(key, (Boolean) value);
        }
    }
}
