package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.InvalidConfigException;
import com.golfing8.kcommon.struct.drop.CommandDrop;
import com.golfing8.kcommon.struct.drop.Drop;
import com.golfing8.kcommon.struct.drop.ItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.MapUtil;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A serializer for {@link Drop}
 */
@SuppressWarnings("rawtypes")
public class CADrop implements ConfigAdapter<Drop> {
    @Override
    public Class<Drop> getAdaptType() {
        return Drop.class;
    }

    @Override
    public Drop toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> primitive = entry.unwrap();
        String displayName = primitive.containsKey("display-name") ? primitive.get("display-name").toString() : null;
        double chance = primitive.containsKey("chance") ?
                (double) ConfigPrimitive.coerceStringToBoxed(primitive.get("chance").toString(), Double.class) :
                100.0D;
        if (primitive.containsKey("items") || primitive.containsKey("item")) {
            boolean giveDirectly = (boolean) primitive.getOrDefault("give-directly", false);
            boolean fancy = (boolean) primitive.getOrDefault("fancy", false);
            boolean playerLocked = (boolean) primitive.getOrDefault("player-locked", false);
            boolean lootingEnabled = (boolean) primitive.getOrDefault("looting-enabled", false);
            String lootingFormula = primitive.getOrDefault("looting-formula", "rand1({LOOTING})").toString();
            if (primitive.containsKey("item")) {
                ItemStackBuilder deserialized = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("item")), ItemStackBuilder.class);
                ItemDrop drop = new ItemDrop(chance, displayName, MapUtil.of("item", deserialized), giveDirectly, fancy, playerLocked, lootingEnabled, lootingFormula);
                drop.set_key(entry.getSource() != null ? entry.getSource().getName() : null);
                return drop;
            } else {
                FieldType fieldType = FieldType.extractFrom(new TypeToken<Map<String, ItemStackBuilder>>() {});
                Map<String, ItemStackBuilder> items = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("items")), fieldType);
                ItemDrop drop = new ItemDrop(chance, displayName, items, giveDirectly, fancy, playerLocked, lootingEnabled, lootingFormula);
                drop.set_key(entry.getSource() != null ? entry.getSource().getName() : null);
                return drop;
            }
        } else if (primitive.containsKey("commands")) {
            FieldType fieldType = FieldType.extractFrom(new TypeToken<List<String>>() {});
            List<String> commands = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("commands")), fieldType);
            CommandDrop drop = new CommandDrop(chance, displayName, commands);
            drop.set_key(entry.getSource() != null ? entry.getSource().getName() : null);
            return drop;
        }
        throw new InvalidConfigException("Drop doesn't have `commands` or `items` key. Which type of drop is it?");
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Drop object) {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("chance", object.getChance());
        if (object.getDisplayName() != null) {
            serialized.put("display-name", object.getDisplayName());
        }

        if (object instanceof ItemDrop) {
            ItemDrop itemDrop = (ItemDrop) object;
            if (itemDrop.getItems().size() == 1) {
                serialized.put("item", ConfigTypeRegistry.toPrimitive(itemDrop.getItems().values().stream().findFirst().get()));
            } else {
                serialized.put("items", ConfigTypeRegistry.toPrimitive(((ItemDrop) object).getItems()));
            }
            serialized.put("give-directly", itemDrop.isGiveDirectly());
            serialized.put("fancy", itemDrop.isFancyDrop());
            serialized.put("player-locked", itemDrop.isPlayerLocked());
            serialized.put("looting-enabled", itemDrop.isLootingEnabled());
            serialized.put("looting-formula", itemDrop.getLootingFormula());
            return ConfigPrimitive.ofMap(serialized);
        } else if (object instanceof CommandDrop) {
            serialized.put("commands", ConfigTypeRegistry.toPrimitive(((CommandDrop) object).getDrop()));
            return ConfigPrimitive.ofMap(serialized);
        }
        throw new IllegalArgumentException(String.format("Drop with type %s doesn't have serializer.", object.getClass().getSimpleName()));
    }
}
