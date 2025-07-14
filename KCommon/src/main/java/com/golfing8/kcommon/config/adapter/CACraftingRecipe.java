package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.util.Reflection;
import com.google.common.collect.Lists;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts all bukkit crafting recipes.
 */
@SuppressWarnings({"unchecked"})
public class CACraftingRecipe implements ConfigAdapter<Recipe> {
    private final MethodHandle getKeyHandle = Reflection.forNameOptional("org.bukkit.inventory.CraftingRecipe").map(clazz -> Reflection.findMethodHandle(clazz, "getKey")).orElse(null);
    private final MethodHandle setItemIngredientHandle = Reflection.findMethodHandle(ShapedRecipe.class, "setIngredient", char.class, ItemStack.class);
    private final MethodHandle addItemIngredientHandle = Reflection.findMethodHandle(ShapelessRecipe.class, "addIngredient", ItemStack.class);
    private final MethodHandle newShapelessRecipeConstructor = Reflection.findConstructor(ShapelessRecipe.class, NamespacedKey.class, ItemStack.class);
    private final MethodHandle newShapedRecipeConstructor = Reflection.findConstructor(ShapedRecipe.class, NamespacedKey.class, ItemStack.class);

    @Override
    public Class<Recipe> getAdaptType() {
        return Recipe.class;
    }

    @Override
    public @Nullable Recipe toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        ItemStackBuilder result = ConfigTypeRegistry.getFromType(entry.getSubValue("result"), ItemStackBuilder.class);
        Map<String, Object> unwrappedData = entry.unwrap();
        if (unwrappedData.get("ingredients") instanceof Map) {
            ShapedRecipe shapedRecipe;
            if (unwrappedData.get("key") instanceof String && newShapedRecipeConstructor != null) {
                NamespacedKey namespacedKey = NamespacedKey.fromString(unwrappedData.get("key").toString());
                shapedRecipe = Reflection.invokeQuietly(newShapedRecipeConstructor, namespacedKey, result.buildFromTemplate());
            } else {
                shapedRecipe = new ShapedRecipe(result.buildFromTemplate());
            }

            List<String> shape = (List<String>) unwrappedData.get("shape");
            shapedRecipe.shape(shape.toArray(new String[0]));

            Map<String, Object> ingredients = entry.getSubValue("ingredients").unwrap();
            ingredients.forEach((k, v) -> {
                if (setItemIngredientHandle != null) {
                    Reflection.invokeQuietly(setItemIngredientHandle, shapedRecipe, k.charAt(0), ConfigTypeRegistry.getFromType(entry.getSubValue("ingredients").getSubValue(k), ItemStackBuilder.class).buildFromTemplate());
                } else {
                    shapedRecipe.setIngredient(k.charAt(0), ConfigTypeRegistry.getFromType(entry.getSubValue("ingredients").getSubValue(k), ItemStackBuilder.class).buildFromTemplate().getType());
                }
            });

            return shapedRecipe;
        } else if (unwrappedData.get("ingredients") instanceof List) {
            ShapelessRecipe shapelessRecipe;
            if (unwrappedData.get("key") instanceof String && newShapelessRecipeConstructor != null) {
                NamespacedKey namespacedKey = NamespacedKey.fromString(unwrappedData.get("key").toString());
                shapelessRecipe = Reflection.invokeQuietly(newShapelessRecipeConstructor, namespacedKey, result.buildFromTemplate());
            } else {
                shapelessRecipe = new ShapelessRecipe(result.buildFromTemplate());
            }

            List<Map<String, Object>> ingredients = entry.getSubValue("ingredients").unwrap();
            for (Map<String, Object> ingredient : ingredients) {
                ItemStack ingredientItem = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofMap(ingredient), ItemStackBuilder.class).buildFromTemplate();
                if (addItemIngredientHandle != null) {
                    Reflection.invokeQuietly(addItemIngredientHandle, shapelessRecipe, ingredientItem);
                } else {
                    shapelessRecipe.addIngredient(ingredientItem.getType());
                }
            }
            return shapelessRecipe;
        } else {
            throw new UnsupportedOperationException("Unknown recipe type. Ingredients not list or map");
        }
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull Recipe object) {
        Map<String, Object> data = new HashMap<>();
        data.put("result", ConfigTypeRegistry.toPrimitive(new ItemStackBuilder(object.getResult())));
        if (getKeyHandle != null) {
            NamespacedKey key = Reflection.invokeQuietly(getKeyHandle, object);
            data.put("key", key.getNamespace() + ":" + key.getKey());
        }

        if (object instanceof ShapelessRecipe) {
            ShapelessRecipe recipe = (ShapelessRecipe) object;
            List<Object> ingredientData = new ArrayList<>();
            recipe.getIngredientList().forEach(item -> {
                ingredientData.add(ConfigTypeRegistry.toPrimitive(new ItemStackBuilder(item)).unwrap());
            });
            data.put("ingredients", ingredientData);
            return ConfigPrimitive.ofMap(data);
        } else if (object instanceof ShapedRecipe) {
            ShapedRecipe recipe = (ShapedRecipe) object;

            Map<String, Object> ingredientMap = new HashMap<>();
            recipe.getIngredientMap().forEach((k, v) -> {
                ingredientMap.put(k.toString(), ConfigTypeRegistry.toPrimitive(new ItemStackBuilder(v)).unwrap());
            });
            data.put("ingredients", ingredientMap);

            List<String> shape = Lists.newArrayList(recipe.getShape());
            data.put("shape", shape);
            return ConfigPrimitive.ofMap(data);
        } else {
            throw new UnsupportedOperationException(object.getClass().getName() + " is not supported.");
        }
    }
}
