package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.config.InvalidConfigException;
import com.golfing8.kcommon.struct.drop.CommandDrop;
import com.golfing8.kcommon.struct.drop.Drop;
import com.golfing8.kcommon.struct.drop.DropTable;
import com.golfing8.kcommon.struct.drop.ItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.MapUtil;
import com.google.gson.reflect.TypeToken;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A serializer for {@link Drop}
 */
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
        String group = primitive.containsKey("group") ? primitive.get("group").toString() : null;
        double chance = primitive.containsKey("chance") ?
                (double) ConfigPrimitive.coerceStringToBoxed(primitive.get("chance").toString(), Double.class) :
                1.0D;
        if (primitive.containsKey("items") || primitive.containsKey("item")) {
            boolean giveDirectly = (boolean) primitive.getOrDefault("give-directly", false);
            if (primitive.containsKey("item")) {
                ItemStackBuilder deserialized = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("item")), ItemStackBuilder.class);
                return new ItemDrop(chance, group, MapUtil.of("item", deserialized), giveDirectly);
            } else {
                FieldType fieldType = FieldType.extractFrom(new TypeToken<Map<String, ItemStackBuilder>>() {});
                Map<String, ItemStackBuilder> items = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("items")), fieldType);
                return new ItemDrop(chance, group, items, giveDirectly);
            }
        } else if (primitive.containsKey("commands")) {
            FieldType fieldType = FieldType.extractFrom(new TypeToken<List<String>>() {});
            List<String> commands = ConfigTypeRegistry.getFromType(ConfigPrimitive.of(primitive.get("commands")), fieldType);
            return new CommandDrop(chance, group, commands);
        }
        throw new InvalidConfigException("Drop doesn't have `commands` or `items` key. Which type of drop is it?");
    }

    @Override
    public ConfigPrimitive toPrimitive(Drop object) {
        if (object == null)
            return ConfigPrimitive.ofNull();

        Map<String, Object> serialized = new HashMap<>();
        serialized.put("chance", object.getChance());
        if (object.getDropGroup() != null) {
            serialized.put("group", object.getDropGroup());
        }

        if (object instanceof ItemDrop) {
            ItemDrop itemDrop = (ItemDrop) object;
            if (itemDrop.getItems().size() == 1) {
                serialized.put("item", ConfigTypeRegistry.toPrimitive(itemDrop.getItems().values().stream().findFirst().get()));
            } else {
                serialized.put("items", ConfigTypeRegistry.toPrimitive(((ItemDrop) object).getItems()));
            }
            return ConfigPrimitive.ofMap(serialized);
        } else if (object instanceof CommandDrop) {
            serialized.put("commands", ConfigTypeRegistry.toPrimitive(((CommandDrop) object).getDrop()));
            return ConfigPrimitive.ofMap(serialized);
        }
        throw new IllegalArgumentException(String.format("Drop with type %s doesn't have serializer.", object.getClass().getSimpleName()));
    }
}
