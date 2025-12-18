package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.item.NMSItemStack;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.Hand;
import com.golfing8.kcommon.nms.struct.PotionData;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * NMS access for items
 */
public interface NMSMagicItems {
    EntityType getSpawnerType(ItemStack stack);

    int getRemainingItemDurability(ItemStack stack);

    boolean setRemainingItemDurability(ItemStack stack, int amount);

    boolean isUnbreakable(ItemMeta meta);

    void setUnbreakable(ItemMeta meta, boolean value);

    void setAttributeModifiers(ItemStack meta, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers);

    void setExtraAttributeModifiers(ItemStack meta, Map<EntityAttribute, Set<EntityAttributeModifier>> modifiers);

    void setSkullOwningPlayer(SkullMeta meta, OfflinePlayer offlinePlayer);

    void setSkullTexture(SkullMeta meta, String base64Texture);

    /**
     * Sets the item model for the given item meta.
     *
     * @param meta the meta
     * @param key  the key
     */
    void setItemModel(ItemMeta meta, @Nullable String key);

    /**
     * Applies the name to the item and tries to use mini message if available.
     *
     * @param meta the meta.
     * @param name the name of the item.
     */
    void applyName(ItemMeta meta, @Nullable String name);

    /**
     * Applies the lore to the given item in mini message format if available.
     *
     * @param meta the item meta.
     * @param lore the new lore.
     */
    void applyLore(ItemMeta meta, @Nullable List<String> lore);

    /**
     * Applies the given name component to the given item meta
     *
     * @param meta the meta
     * @param component the name component
     */
    void applyComponentName(ItemMeta meta, @Nullable Component component);

    /**
     * Applies the given lore component to the given item meta
     *
     * @param meta the meta
     * @param components the lore component
     */
    void applyComponentLore(ItemMeta meta, @Nullable List<? extends Component> components);

    /**
     * Gets a mini message serialized item name.
     *
     * @param meta the meta.
     * @return the mini message formatted display name.
     */
    String getMMDisplayName(ItemMeta meta);

    /**
     * Gets the mini message serialized lore.
     *
     * @param meta the meta of the item.
     * @return the mini message formatted lore.
     */
    List<String> getMMLore(ItemMeta meta);

    /**
     * Gets the component display name of the given item meta
     *
     * @param meta the item meta
     * @return the display name component
     */
    @Nullable Component getComponentDisplayName(ItemMeta meta);

    /**
     * Gets the component lore of the given item meta
     *
     * @param meta the meta
     * @return the lore components
     */
    @Nullable List<Component> getComponentLore(ItemMeta meta);

    /**
     * Gets the displayed name of the given item.
     *
     * @return the displayed name.
     */
    String getDisplayName(ItemStack itemStack);

    /**
     * Sets the base potion effect of the given potion.
     *
     * @param meta the meta.
     */
    default void setBaseEffect(PotionMeta meta, PotionData potionData) {
        meta.setBasePotionData(new org.bukkit.potion.PotionData(potionData.getPotionType(), potionData.isExtended(), potionData.isAmplified()));
    }

    /**
     * Gets the base potion effect of the given potion meta.
     *
     * @param meta the meta.
     * @return the potion data.
     */
    default PotionData getBaseEffect(PotionMeta meta) {
        org.bukkit.potion.PotionData potionData = meta.getBasePotionData();
        return new PotionData(potionData.getType(), potionData.isUpgraded(), potionData.isExtended());
    }

    /**
     * Sets the custom model data for the given item meta.
     *
     * @param meta      the item meta.
     * @param modelData the custom model data.
     */
    void setCustomModelData(ItemMeta meta, int modelData);

    default void setItemInHand(Player player, Hand hand, ItemStack stack) {
        switch (hand) {
            case MAIN:
                player.getInventory().setItemInMainHand(stack);
                return;
            case OFF_HAND:
                player.getInventory().setItemInOffHand(stack);
        }
    }

    default ItemStack getItemInHand(Player player, Hand hand) {
        switch (hand) {
            case MAIN:
                return player.getInventory().getItemInMainHand();
            case OFF_HAND:
                return player.getInventory().getItemInOffHand();
        }
        return null;
    }

    /**
     * Wraps the given item stack.
     *
     * @param itemStack the item stack.
     * @return the wrapped item stack.
     */
    NMSItemStack wrapItemStack(ItemStack itemStack);

    default void setUnbreakable(ItemStack itemStack, boolean value) {
        if (itemStack == null || !itemStack.hasItemMeta())
            return;

        setUnbreakable(itemStack.getItemMeta(), value);
    }

    void setUnstackable(ItemStack itemStack, boolean value);

    static GameProfile makeProfile(String b64) {
        // random uuid based on the b64 string
        UUID id = new UUID(
                b64.substring(b64.length() - 20).hashCode(),
                b64.substring(b64.length() - 10).hashCode()
        );
        GameProfile profile = new GameProfile(id, "Player");
        profile.getProperties().put("textures", new Property("textures", b64));
        return profile;
    }
}
