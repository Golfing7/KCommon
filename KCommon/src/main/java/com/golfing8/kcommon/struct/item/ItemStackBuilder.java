package com.golfing8.kcommon.struct.item;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.deanveloper.skullcreator.SkullCreator;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.exc.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.PotionData;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.ItemUtil;
import com.golfing8.kcommon.util.MS;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.NBTItem;
import lombok.AccessLevel;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A builder for a bukkit item stack. Can be used as a factory for item templates as well.
 */
@Getter
public final class ItemStackBuilder {
    public static final String ITEMSTACK_ID = "kcommon_id";
    /**
     * A string ID to use in reference to this item.
     */
    private @Nullable String itemID;
    /**
     * The type of the item stack.
     */
    private XMaterial itemType;
    /**
     * Used on newer item stacks to apply a custom model data.
     */
    private int customModelData = 0;
    /**
     * The model used for the item. 1.21.4+.
     */
    private @Nullable String itemModel;
    /**
     * The amount of items in the stack.
     */
    private int amount = 1;
    /**
     * An amount for the item, will override {@link #amount} if set.
     */
    private Range variableAmount;
    private int newAmount() {
        return variableAmount == null ? amount : variableAmount.getRandomI();
    }
    /**
     * Item durability, should only be used in cases of older versions.
     */
    private short itemDurability;
    /**
     * If this item is unbreakable or not.
     */
    private boolean unbreakable;
    /** If the item can be made shiny */
    private boolean glowing;
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
    /** The potion data for this item */
    private PotionData potionData;
    /** Stores attributes modifiers for the item */
    private Map<EntityAttribute, Set<EntityAttributeModifier>> attributeModifierMap = new HashMap<>();
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
     * Overrides for the item's components
     */
    private Map<String, Object> components = new HashMap<>();
    /**
     * The skull's base 64 texture. Only applied if this item builder builds a player head.
     */
    private String skullB64;
    /**
     * The UUID of the owner of the skull. Only applied if this item builder builds a player head.
     */
    private UUID skullOwner;
    /**
     * The last built itemstack for this builder.
     */
    @Getter(AccessLevel.PRIVATE)
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
        this.unbreakable = NMS.getTheNMS().getMagicItems().isUnbreakable(itemMeta);
        this.itemName = itemMeta.hasDisplayName() ? itemMeta.getDisplayName() : null;
        this.itemLore = itemMeta.hasLore() ? itemMeta.getLore() : new ArrayList<>();
        Map<Enchantment, Integer> enchants = itemMeta.getEnchants();
        for (Map.Entry<Enchantment, Integer> enchant : enchants.entrySet()) {
            this.enchant(XEnchantment.matchXEnchantment(enchant.getKey()), enchant.getValue());
        }
        this.itemFlags = itemMeta.getItemFlags();
    }

    public ItemStackBuilder(ItemStackBuilder toCopy) {
        this.itemID = toCopy.itemID;
        this.itemType = toCopy.itemType;
        this.amount = toCopy.amount;
        this.customModelData = toCopy.customModelData;
        this.itemModel = toCopy.itemModel;
        this.cachedBuild = toCopy.cachedBuild;
        this.variableAmount = toCopy.variableAmount;
        this.itemDurability = toCopy.itemDurability;
        this.unbreakable = toCopy.unbreakable;
        this.itemName = toCopy.itemName;
        this.itemLore = new ArrayList<>(toCopy.itemLore);
        this.enchantments = new HashMap<>(toCopy.enchantments);
        this.itemFlags = new HashSet<>(toCopy.itemFlags);
        this.placeholders = new ArrayList<>(toCopy.placeholders);
        this.multiLinePlaceholders = new ArrayList<>(toCopy.multiLinePlaceholders);
        this.skullB64 = toCopy.skullB64;
        this.extraData = new HashMap<>(toCopy.extraData);
        this.components = new HashMap<>(toCopy.components);
        this.potionData = toCopy.potionData;
        this.glowing = toCopy.glowing;
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
        this.itemID = section.getString("item-id");
        this.glowing = section.getBoolean("glowing");
        this.skullB64 = section.getString("skull-texture");
        this.itemDurability = section.contains("durability") ? (short) section.getInt("durability") : this.itemType.getData();
        this.unbreakable = section.getBoolean("unbreakable", false);
        this.customModelData = section.getInt("custom-model-data", 0);
        this.itemModel = section.getString("item-model");
        this.itemName = section.getString("name", null);
        this.itemLore = section.contains("lore") ? section.getStringList("lore") : null;
        this.amount = Math.max(section.getInt("amount", 1), 1);
        if (section.contains("nbt-data")) {
            this.extraData = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "nbt-data"), FieldType.extractFrom(new TypeToken<Map<String, Object>>() {}));
        }
        if (section.contains("components")) {
            this.components = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "components"), FieldType.extractFrom(new TypeToken<Map<String, Object>>() {}));
        }
        if (section.contains("variable-amount")) {
            this.variableAmount = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "variable-amount"), Range.class);
        }

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

    public ItemStackBuilder skullB64(String b64) {
        this.skullB64 = b64;
        return this;
    }

    public ItemStackBuilder skullOwner(UUID skullOwner) {
        this.skullOwner = skullOwner;
        return this;
    }

    public ItemStackBuilder itemID(String id) {
        this.itemID = id;
        return this;
    }

    public ItemStackBuilder material(XMaterial material) {
        this.itemType = material;
        return this;
    }

    public ItemStackBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemStackBuilder variableAmount(Range range) {
        this.variableAmount = range;
        return this;
    }

    public ItemStackBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemStackBuilder glowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public ItemStackBuilder name(String name) {
        this.itemName = name;
        return this;
    }

    public ItemStackBuilder potionData(PotionData data) {
        this.potionData = data;
        return this;
    }

    public ItemStackBuilder attributeModifierMap(Map<EntityAttribute, Set<EntityAttributeModifier>> attributeMap) {
        this.attributeModifierMap = attributeMap;
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
        if (this.itemLore == null) {
            this.itemLore = new ArrayList<>();
        }
        for (int i = 0; i < lore.length; i++) {
            this.itemLore.add(index + i, lore[i]);
        }
        return this;
    }

    public ItemStackBuilder addLore(String... lore) {
        if (this.itemLore == null) {
            this.itemLore = new ArrayList<>();
        }
        this.itemLore.addAll(Arrays.asList(lore));
        return this;
    }

    public ItemStackBuilder addLore(Collection<String> lore) {
        if (this.itemLore == null) {
            this.itemLore = new ArrayList<>();
        }
        this.itemLore.addAll(lore);
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

    public ItemStackBuilder placeholders(Collection<Placeholder> placeholders) {
        this.placeholders = new ArrayList<>(placeholders);
        return this;
    }

    public ItemStackBuilder addPlaceholders(Placeholder... placeholders) {
        if (this.placeholders == null) {
            this.placeholders = Lists.newArrayList(placeholders);
        } else {
            this.placeholders.addAll(Arrays.asList(placeholders));
        }
        return this;
    }

    public ItemStackBuilder addPlaceholders(Collection<Placeholder> placeholders) {
        if (this.placeholders == null) {
            this.placeholders = Lists.newArrayList(placeholders);
        } else {
            this.placeholders.addAll(placeholders);
        }
        return this;
    }

    public ItemStackBuilder multiLinePlaceholders(MultiLinePlaceholder... placeholders) {
        this.multiLinePlaceholders = Lists.newArrayList(placeholders);
        return this;
    }

    public ItemStackBuilder multiLinePlaceholders(Collection<MultiLinePlaceholder> placeholders) {
        this.multiLinePlaceholders = new ArrayList<>(placeholders);
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

    public ItemStackBuilder components(Map<String, Object> data) {
        this.components = data == null ? Collections.emptyMap() : new HashMap<>(data);
        return this;
    }

    public ItemStackBuilder itemModel(@Nullable String itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    /**
     * Builds an item stack from the given template.
     *
     * @param placeholderTarget the target for parsing placeholderapi placeholders.
     * @return the item stack built from the template.
     */
    public ItemStack buildFromTemplate(@Nullable Player placeholderTarget) {
        ItemStack newCopy;
        Placeholder[] placeholderArr = placeholders.toArray(new Placeholder[0]);
        if (itemType == XMaterial.PLAYER_HEAD) {
            if (skullB64 != null) {
                newCopy = SkullCreator.itemWithBase64(XMaterial.PLAYER_HEAD.parseItem(), MS.parseSingle(skullB64, placeholderArr));
            } else if (skullOwner != null) {
                newCopy = SkullCreator.itemFromUuid(skullOwner);
            } else {
                newCopy = itemType.parseItem();
            }
        } else {
            newCopy = itemType.parseItem();
            if (itemDurability > 0) {
                newCopy.setDurability(itemDurability);
            }
        }
        newCopy.setAmount(newAmount());

        NMS.getTheNMS().getMagicItems().setAttributeModifiers(newCopy, attributeModifierMap);

        ItemMeta meta = newCopy.getItemMeta();
        if (itemModel != null) {
            NMS.getTheNMS().getMagicItems().setItemModel(meta, itemModel);
        }

        if(this.itemName != null) {
            String itemName = this.itemName;
            if (placeholderTarget != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                itemName = PlaceholderAPI.setPlaceholders(placeholderTarget, itemName);
            }
            NMS.getTheNMS().getMagicItems().applyName(meta, MS.parseSingle(itemName, placeholderArr));
        }
        if(this.itemLore != null && !this.itemLore.isEmpty()) {
            //Parse both single and multi placeholders.
            List<String> firstRun = MS.parseAll(this.itemLore, placeholderArr);
            List<String> secondRun = MS.parseAllMulti(firstRun, multiLinePlaceholders.toArray(new MultiLinePlaceholder[0]));
            if (placeholderTarget != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                secondRun = PlaceholderAPI.setPlaceholders(placeholderTarget, secondRun);
            }
            NMS.getTheNMS().getMagicItems().applyLore(meta, secondRun);
        }

        if (meta instanceof PotionMeta && potionData != null) {
            PotionMeta potionMeta = (PotionMeta) meta;
            NMS.getTheNMS().getMagicItems().setBaseEffect(potionMeta, potionData);
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

        if (this.glowing) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addEnchant(XEnchantment.UNBREAKING.getEnchant(), 0, true);
        }

        newCopy.setItemMeta(meta);

        //Add the nbt item and extra data.
        if (KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_21)) {
            NBT.modifyComponents(newCopy, (nbt) -> {
                ItemUtil.setInNBT(nbt, this.components);
            });
        }
        NBT.modify(newCopy, (nbt) -> {
            if (this.itemID != null) {
                nbt.setString(ITEMSTACK_ID, this.itemID);
            }
            ItemUtil.setInNBT(nbt, this.extraData);
        });

        cachedBuild = newCopy;
        return newCopy;
    }

    /**
     * Builds an item stack from the given template.
     *
     * @return the item stack built from the template.
     */
    public ItemStack buildFromTemplate() {
        return this.buildFromTemplate(null);
    }

    /**
     * Returns a copy of the cached itemstack, being the last built itemstack from this builder.
     *
     * @param placeholderTarget the target for parsing placeholderapi.
     * @return the cached itemstack.
     */
    public ItemStack buildCached(@Nullable Player placeholderTarget) {
        if (cachedBuild == null)
            buildFromTemplate(placeholderTarget);

        return new ItemStack(cachedBuild);
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

    /**
     * Reads the item id from the given item stack.
     *
     * @param stack the item stack.
     * @return the item ID, or null if there is no ID present.
     */
    public static @Nullable String readItemID(ItemStack stack) {
        if (stack == null || !stack.hasItemMeta())
            return null;

        NBTItem nbtItem = new NBTItem(stack);
        if (!nbtItem.hasTag(ITEMSTACK_ID))
            return null;

        return nbtItem.getString(ITEMSTACK_ID);
    }
}
