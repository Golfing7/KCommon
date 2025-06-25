package com.golfing8.kcommon.config.adapter;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.PotionData;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.gson.reflect.TypeToken;
import lombok.var;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A config adapter for bukkit {@link ItemStackBuilder} instances.
 * <p>
 * This class uses the {@link ItemStackBuilder} class to build instances from the config.
 * </p>
 */
public class CAItemStackBuilder implements ConfigAdapter<ItemStackBuilder> {
    @Override
    public Class<ItemStackBuilder> getAdaptType() {
        return ItemStackBuilder.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ItemStackBuilder toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        ItemStackBuilder builder = new ItemStackBuilder();
        Map<String, Object> primitiveValue = (Map<String, Object>) entry.unwrap();
        builder.itemType(primitiveValue.get("type").toString());
        if (primitiveValue.containsKey("amount"))
            builder.amount((int) primitiveValue.get("amount"));
        if (primitiveValue.containsKey("durability"))
            builder.durability(((Integer) primitiveValue.get("durability")).shortValue());
        if (primitiveValue.containsKey("unbreakable"))
            builder.unbreakable((boolean) primitiveValue.get("unbreakable"));
        if (primitiveValue.containsKey("custom-model-data"))
            builder.customModelData((int) primitiveValue.get("custom-model-data"));
        if (primitiveValue.containsKey("item-model"))
            builder.itemModel((String) primitiveValue.get("item-model"));
        if (primitiveValue.containsKey("name"))
            builder.name(primitiveValue.get("name").toString());
        if (primitiveValue.containsKey("lore"))
            builder.lore((List<String>) primitiveValue.get("lore"));
        if (primitiveValue.containsKey("nbt-data"))
            builder.extraData((Map<String, Object>) primitiveValue.get("nbt-data"));
        if (primitiveValue.containsKey("components"))
            builder.components((Map<String, Object>) primitiveValue.get("components"));
        if (primitiveValue.containsKey("potion-data"))
            builder.potionData(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofTrusted(primitiveValue.get("potion-data")), new FieldType(PotionData.class)));
        if (primitiveValue.containsKey("glowing"))
            builder.glowing((Boolean) primitiveValue.get("glowing"));
        if (primitiveValue.containsKey("skull-texture"))
            builder.skullB64((String) primitiveValue.get("skull-texture"));
        if (primitiveValue.containsKey("variable-amount"))
            builder.variableAmount(ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitiveValue.get("variable-amount")), Range.class));
        if (primitiveValue.containsKey("item-id"))
            builder.itemID(primitiveValue.get("item-id").toString());
        if (primitiveValue.containsKey("enchantments")) {
            Map<String, Object> enchantments = (Map<String, Object>) primitiveValue.get("enchantments");
            for (Map.Entry<String, Object> enchant : enchantments.entrySet()) {
                var enchantment = XEnchantment.matchXEnchantment(enchant.getKey());
                if (!enchantment.isPresent())
                    continue;

                int level = (int) enchant.getValue();
                builder.enchant(enchantment.get(), level);
            }
        }
        if (primitiveValue.containsKey("flags")) {
            List<String> flags = (List<String>) primitiveValue.get("flags");
            builder.flags(flags.stream().map(ItemFlag::valueOf).toArray(ItemFlag[]::new));
        }
        if (primitiveValue.containsKey("attribute-modifiers")) {
            ConfigPrimitive subValue = entry.getSubValue("attribute-modifiers");
            builder.attributeModifierMap(ConfigTypeRegistry.getFromType(subValue, FieldType.extractFrom(new TypeToken<Map<EntityAttribute, Set<EntityAttributeModifier>>>() {})));
        }

        return builder;
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull ItemStackBuilder builder) {
        Map<String, Object> objects = new HashMap<>();
        objects.put("type", builder.getItemTypeString());
        objects.put("amount", builder.getAmount());
        if (builder.getItemDurability() != 0)
            objects.put("durability", builder.getItemDurability());
        if (builder.isUnbreakable())
            objects.put("unbreakable", true);
        if (builder.getItemName() != null)
            objects.put("name", builder.getItemName());
        if (builder.getItemLore() != null && !builder.getItemLore().isEmpty())
            objects.put("lore", builder.getItemLore());
        if (builder.getExtraData() != null && !builder.getExtraData().isEmpty())
            objects.put("nbt-data", builder.getExtraData());
        if (builder.getComponents() != null && !builder.getComponents().isEmpty())
            objects.put("components", builder.getComponents());
        if (builder.getPotionData() != null)
            objects.put("potion-data", ConfigTypeRegistry.toPrimitive(builder.getPotionData()).unwrap());
        if (builder.getCustomModelData() != 0)
            objects.put("custom-model-data", builder.getCustomModelData());
        if (builder.getItemModel() != null)
            objects.put("item-model", builder.getItemModel());
        if (builder.isGlowing())
            objects.put("glowing", true);
        if (builder.getSkullB64() != null)
            objects.put("skull-texture", builder.getSkullB64());
        if (builder.getVariableAmount() != null)
            objects.put("variable-amount", ConfigTypeRegistry.toPrimitive(builder.getVariableAmount()));
        if (builder.getItemID() != null)
            objects.put("item-id", builder.getItemID());
        if (builder.getEnchantments() != null && !builder.getEnchantments().isEmpty()) {
            Map<String, Integer> enchantments = new HashMap<>();
            builder.getEnchantments().forEach((k, v) -> {
                enchantments.put(k.name(), v);
            });
            objects.put("enchantments", enchantments);
        }
        if (!builder.getItemFlags().isEmpty())
            objects.put("flags", builder.getItemFlags().stream().map(ItemFlag::name).collect(Collectors.toList()));
        if (!builder.getAttributeModifierMap().isEmpty())
            objects.put("attribute-modifiers", ConfigTypeRegistry.toPrimitive(builder.getAttributeModifierMap()).unwrap());
        return ConfigPrimitive.ofMap(objects);
    }
}
