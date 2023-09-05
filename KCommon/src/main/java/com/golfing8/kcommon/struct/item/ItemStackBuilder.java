package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.MS;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * A builder for a bukkit item stack. Can be used as a factory for item templates as well.
 */
@Getter
public final class ItemStackBuilder {
    /**
     * The type of the item stack.
     */
    private XMaterial itemType;
    /**
     * Used on newer item stacks to apply a custom model data.
     */
    private int customModelData = 0;
    /**
     * The amount of items in the stack.
     */
    private int amount = 1;
    /**
     * Item durability, should only be used in cases of older versions.
     */
    private short itemDurability;
    /**
     * If this item is unbreakable or not.
     */
    private boolean unbreakable;
    /**
     * The name of the item.
     */
    private String itemName;
    /**
     * The lore of the item.
     */
    private List<String> itemLore = new ArrayList<>();
    /**
     * The enchants to apply to the item.
     */
    private Map<XEnchantment, Integer> enchantments = new HashMap<>();
    /**
     * The item flags to apply to the item.
     */
    private Set<ItemFlag> itemFlags = new HashSet<>();
    /**
     * The placeholders for the item, applied to all parts.
     */
    private List<Placeholder> placeholders = new ArrayList<>();
    /**
     * Placeholders that will be applied specifically to the lore of the item.
     */
    private List<MultiLinePlaceholder> multiLinePlaceholders = new ArrayList<>();
    /**
     * Extra data to apply to the item via NBT storage.
     */
    private Map<String, Object> extraData = new HashMap<>();
    /**
     * The last built itemstack for this builder.
     */
    private ItemStack cachedBuild;

    /**
     * Default constructor useful for building.
     */
    public ItemStackBuilder(){/*Intentionally empty*/}

    /**
     * Generates an instance from the given bukkit item stack.
     *
     * @param itemStack the item stack.
     */
    public ItemStackBuilder(ItemStack itemStack) {
        this.itemType = XMaterial.matchXMaterial(itemStack.getType());
        this.amount = itemStack.getAmount();
        this.itemDurability = itemStack.getDurability();
        ItemMeta itemMeta = itemStack.getItemMeta();
        this.unbreakable = itemMeta.isUnbreakable();
        this.itemName = itemStack.getItemMeta().getDisplayName();
        this.itemLore = itemMeta.getLore();
        Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
        for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
            this.enchant(XEnchantment.matchXEnchantment(enchant.getKey()), enchant.getValue());
        }
        this.itemFlags = itemMeta.getItemFlags();
    }

    /**
     * Loads this item stack builder from the given config section.
     *
     * @param section the section to load from.
     */
    public ItemStackBuilder(ConfigurationSection section) {
        Optional<XMaterial> optionalType = XMaterial.matchXMaterial(section.getString("type"));
        if(!optionalType.isPresent()) {
            throw new ImproperlyConfiguredValueException(section, "type");
        }

        this.itemType = optionalType.get();
        this.placeholders = new ArrayList<>();
        this.multiLinePlaceholders = new ArrayList<>();
        this.itemDurability = section.contains("durability") ? (short) section.getInt("durability") : this.itemType.getData();
        this.unbreakable = section.getBoolean("unbreakable", false);
        this.customModelData = section.getInt("custom-model-data", 0);
        this.itemName = section.getString("name", null);
        this.itemLore = section.contains("lore") ? section.getStringList("lore") : null;
        this.amount = Math.max(section.getInt("amount", 1), 1);

        //Load the enchantments.
        if(section.contains("enchantments")) {
            ConfigurationSection enchSection = section.getConfigurationSection("enchantments");
            enchSection.getKeys(false).forEach(key -> {
                //Check that the enchantment is actually defined.
                Optional<XEnchantment> optionalEnchantment = XEnchantment.matchXEnchantment(key);
                if(!optionalEnchantment.isPresent()) {
                    throw new ImproperlyConfiguredValueException(enchSection, key);
                }

                //Add the enchantment to the map.
                XEnchantment enchantment = optionalEnchantment.get();
                this.enchantments.put(enchantment, enchSection.getInt(key));
            });
        }

        //Load the item flags.
        if(section.contains("flags")) {
            section.getStringList("flags").forEach(flag -> {
                try{
                    ItemFlag itemFlag = ItemFlag.valueOf(flag);
                    this.itemFlags.add(itemFlag);
                }catch(IllegalArgumentException exc) {
                    throw new ImproperlyConfiguredValueException(section, "flags");
                }
            });
        }
    }

    /*
    Builder methods for the above fields.
     */

    public ItemStackBuilder material(XMaterial material) {
        this.itemType = material;
        return this;
    }

    public ItemStackBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStackBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemStackBuilder name(String name) {
        this.itemName = name;
        return this;
    }

    public ItemStackBuilder lore(String... lore) {
        this.itemLore = lore == null ? Collections.emptyList() : Arrays.asList(lore);
        return this;
    }

    public ItemStackBuilder lore(Collection<String> lore) {
        this.itemLore = lore == null ? Collections.emptyList() : new ArrayList<>(lore);
        return this;
    }

    public ItemStackBuilder insertLore(int index, Collection<String> lore) {
        return this.insertLore(index, lore.toArray(new String[0]));
    }

    public ItemStackBuilder insertLore(int index, String... lore) {
        for (int i = 0; i < lore.length; i++) {
            this.itemLore.add(index + i, lore[i]);
        }
        return this;
    }

    public ItemStackBuilder durability(short dura) {
        this.itemDurability = dura;
        return this;
    }

    public ItemStackBuilder customModelData(int customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemStackBuilder enchant(XEnchantment enchantment, int level) {
        this.enchantments.put(enchantment, level);
        return this;
    }

    public ItemStackBuilder enchants(Map<XEnchantment, Integer> enchantments) {
        this.enchantments = enchantments == null ? Collections.emptyMap() : enchantments;
        return this;
    }

    public ItemStackBuilder flags(ItemFlag... flags) {
        this.itemFlags = flags == null ? Collections.emptySet() : Sets.newHashSet(flags);
        return this;
    }

    public ItemStackBuilder placeholders(Placeholder... placeholders) {
        this.placeholders = Lists.newArrayList(placeholders);
        return this;
    }

    public ItemStackBuilder multiLinePlaceholders(MultiLinePlaceholder... placeholders) {
        this.multiLinePlaceholders = Lists.newArrayList(placeholders);
        return this;
    }

    public ItemStackBuilder extraData(String key, String value) {
        this.extraData.put(key, value);
        return this;
    }

    public ItemStackBuilder extraData(Map<String, Object> data) {
        this.extraData = data == null ? Collections.emptyMap() : new HashMap<>(data);
        return this;
    }

    /**
     * Builds an item stack from the given template.
     *
     * @return the item stack built from the template.
     */
    public ItemStack buildFromTemplate() {
        ItemStack newCopy = new ItemStack(itemType.parseMaterial(), amount, itemDurability);
        if (itemType.getData() != 0) {
            newCopy.setDurability(itemType.getData());
        }

        ItemMeta meta = newCopy.getItemMeta();

        Placeholder[] placeholderArr = placeholders.toArray(new Placeholder[0]);
        if(this.itemName != null)
            meta.setDisplayName(MS.parseSingle(this.itemName, placeholderArr));
        if(this.itemLore != null && !this.itemLore.isEmpty()) {
            //Parse both single and multi placeholders.
            List<String> firstRun = MS.parseAll(this.itemLore, placeholderArr);
            List<String> secondRun = MS.parseAllMulti(firstRun, multiLinePlaceholders.toArray(new MultiLinePlaceholder[0]));
            meta.setLore(secondRun);
        }

        //Next, apply item flags
        if(this.itemFlags != null)
            meta.addItemFlags(this.itemFlags.toArray(new ItemFlag[0]));

        //Then, enchants
        if(this.enchantments != null) {
            this.enchantments.forEach((ench, level) -> meta.addEnchant(ench.getEnchant(), level, true));
        }

        //Now, try to apply the custom model data.
        if(this.customModelData != 0) {
            NMS.getTheNMS().getMagicItems().setCustomModelData(meta, this.customModelData);
        }

        if(this.itemDurability > 0) {
            newCopy.setDurability(this.itemDurability);
        }

        //Set the item as unbreakable or not.
        if (this.unbreakable) {
            NMS.getTheNMS().getMagicItems().setUnbreakable(meta, this.unbreakable);
        }

        newCopy.setItemMeta(meta);

        //Add the nbt item and extra data.
        NBTItem nbtItem = new NBTItem(newCopy);
        for(Map.Entry<String, Object> entry : this.extraData.entrySet()) {
            setValueInNBT(nbtItem, entry.getKey(), entry.getValue());
        }
        cachedBuild = newCopy = nbtItem.getItem();
        return newCopy;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void setValueInNBT(NBTItem nbtItem, String key, Object value) {
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
            ((Map) value).forEach((k, v) -> {
                setValueInNBT(nbtItem, k.toString(), v);
            });
        } else if (value instanceof Boolean) {
            nbtItem.setBoolean(key, (Boolean) value);
        }
    }

    /**
     * Returns a copy of the cached itemstack, being the last built itemstack from this builder.
     *
     * @return the cached itemstack.
     */
    public ItemStack buildCached() {
        if (cachedBuild == null)
            buildFromTemplate();

        return new ItemStack(cachedBuild);
    }
}
