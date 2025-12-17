package com.golfing8.kcommon.util;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.nms.ItemCapturePlayer;
import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.struct.placeholder.PlaceholderContainer;
import com.google.common.collect.Lists;
import de.tr7zw.changeme.nbtapi.NBTType;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import lombok.experimental.UtilityClass;
import lombok.var;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utilities for {@link ItemStack} objects.
 */
@UtilityClass
public final class ItemUtil {

    /**
     * Dispatches the command as console and captures any item output.
     * <p>
     * The command should send the item to a player named {@code _}, otherwise it won't provide an item.
     * </p>
     *
     * @param command the command.
     * @return the item.
     */
    public static Optional<ItemStack> getItemFromCommand(String command) {
        if (!Bukkit.isPrimaryThread())
            throw new IllegalStateException("Cannot get item from command asynchronously!");

        ItemCapturePlayer capturePlayer = NMS.getTheNMS().createPlayerForItemCapture();
        try {
            AtomicReference<ItemStack> reference = new AtomicReference<>();
            capturePlayer.getInventoryItemHooks().add(reference::set);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            // We may have to **force** the command execution to continue.
            NMS.getTheNMS().flushCommandQueue();
            return Optional.ofNullable(reference.get());
        } finally {
            // Clean up after ourselves.
            NMS.getTheNMS().removeItemCapturePlayer(capturePlayer);
        }
    }

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
     * Gets the display name of the item with the amount
     *
     * @param itemStack the itemstack.
     * @return its displayed name with the amount of the item
     */
    public static String getDisplayNameWithAmount(ItemStack itemStack) {
        if (itemStack == null)
            return "1x Air";

        if (itemStack.hasItemMeta() && itemStack.getItemMeta().hasDisplayName()) {
            return itemStack.getAmount() + "x " + itemStack.getItemMeta().getDisplayName();
        } else {
            NMSItemStack stack = NMS.getTheNMS().getMagicItems().wrapItemStack(itemStack);
            return itemStack.getAmount() + "x " + stack.getI18DisplayName();
        }
    }

    /**
     * Applies the placeholders to the given item.
     *
     * @param itemStack    the item
     * @param placeholders the placeholders
     */
    public static void applyPlaceholders(ItemStack itemStack, Object... placeholders) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        ItemMeta meta = itemStack.getItemMeta();
        PlaceholderContainer container = PlaceholderContainer.compileTrusted(placeholders);
        if (meta.hasDisplayName()) {
            Component displayName = NMS.getTheNMS().getMagicItems().getComponentDisplayName(meta);
            List<Component> displayNameComponents = container.applyComponentsUntrusted(container.applyComponentsTrusted(Lists.newArrayList(displayName)));
            NMS.getTheNMS().getMagicItems().applyComponentName(meta, displayNameComponents.isEmpty() ? null : displayNameComponents.get(0));
        }

        if (!meta.hasLore()) {
            itemStack.setItemMeta(meta);
            return;
        }

        List<Component> lore = NMS.getTheNMS().getMagicItems().getComponentLore(meta);
        List<Component> applied = container.applyComponentsUntrusted(container.applyComponentsTrusted(lore));
        NMS.getTheNMS().getMagicItems().applyComponentLore(meta, applied);
        itemStack.setItemMeta(meta);
    }

    /**
     * Checks if the item is air or null or has an amount of 0.
     *
     * @param check the check.
     * @return true if the item is air or null.
     */
    public static boolean isAirOrNull(ItemStack check) {
        return check == null || check.getType() == XMaterial.AIR.get() || check.getAmount() <= 0;
    }

    /**
     * Removes the given data in the given NBT object
     *
     * @param nbt the nbt object
     * @param data the data
     */
    public static void removeInNBT(ReadWriteNBT nbt, Map<String, Object> data) {
        for (var entry : data.entrySet()) {
            nbt.removeKey(entry.getKey());
        }
    }

    /**
     * Removes the given key value specific pair in the given nbt object
     *
     * @param nbt the nbt object
     * @param key the key
     * @param value the value
     */
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

    /**
     * Sets the values in the given nbt object
     *
     * @param nbt the nbt
     * @param data the data
     */
    public static void setInNBT(ReadWriteNBT nbt, Map<String, Object> data) {
        for (var entry : data.entrySet()) {
            setValueInNBT(nbt, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Sets the key/value pair in the given nbt object
     *
     * @param nbt the nbt object
     * @param key the key
     * @param value the value
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueInNBT(ReadWriteNBT nbt, String key, Object value) {
        if (value instanceof Integer) {
            nbt.setInteger(key, (Integer) value);
        } else if (value instanceof Float) {
            nbt.setFloat(key, (Float) value);
        } else if (value instanceof Double) {
            nbt.setDouble(key, (Double) value);
        } else if (value instanceof Byte) {
            nbt.setByte(key, (Byte) value);
        } else if (value instanceof Short) {
            nbt.setShort(key, (Short) value);
        } else if (value instanceof Long) {
            nbt.setLong(key, (Long) value);
        } else if (value instanceof String) {
            nbt.setString(key, (String) value);
        } else if (value instanceof Map) {
            var subCompound = nbt.getOrCreateCompound(key);
            ((Map) value).forEach((k, v) -> {
                setValueInNBT(subCompound, k.toString(), v);
            });
        } else if (value instanceof Boolean) {
            nbt.setBoolean(key, (Boolean) value);
        }
    }
}
