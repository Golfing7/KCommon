package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.filter.ItemFilter;
import com.golfing8.kcommon.struct.filter.StringFilter;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Adapts all {@link ItemFilter} objects.
 */
public class CAItemFilter implements ConfigAdapter<ItemFilter> {
    @Override
    public Class<ItemFilter> getAdaptType() {
        return ItemFilter.class;
    }

    @Override
    public ItemFilter toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        TypeToken<?> token = new TypeToken<Set<StringFilter>>() {
        };
        FieldType ftype = FieldType.extractFrom(token);

        Map<String, Object> objectMap = entry.unwrap();
        Set<StringFilter> materialFilters = Collections.emptySet();
        Set<StringFilter> nameFilters = Collections.emptySet();
        Set<StringFilter> loreFilters = Collections.emptySet();
        if (objectMap.containsKey("material-filters")) {
            materialFilters = ConfigTypeRegistry.getFromType(
                    ConfigPrimitive.ofTrusted(objectMap.get("material-filters")),
                    ftype
            );
        }

        if (objectMap.containsKey("name-filters")) {
            nameFilters = ConfigTypeRegistry.getFromType(
                    ConfigPrimitive.ofTrusted(objectMap.get("name-filters")),
                    ftype
            );
        }

        if (objectMap.containsKey("lore-filters")) {
            loreFilters = ConfigTypeRegistry.getFromType(
                    ConfigPrimitive.ofTrusted(objectMap.get("lore-filters")),
                    ftype
            );
        }

        boolean stripColors = (boolean) objectMap.getOrDefault("strip-colors", false);
        return new ItemFilter(materialFilters, nameFilters, loreFilters, stripColors);
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchekced"})
    public ConfigPrimitive toPrimitive(@NotNull ItemFilter object) {
        Map<String, Object> items = new HashMap<>();
        ConfigAdapter adapter = ConfigTypeRegistry.findAdapter(Set.class);
        if (object.getMaterialFilters() != null && !object.getMaterialFilters().isEmpty()) {
            items.put("material-filters", adapter.toPrimitive(object.getMaterialFilters()).getPrimitive());
        }

        if (object.getItemNameFilters() != null && !object.getItemNameFilters().isEmpty()) {
            items.put("name-filters", adapter.toPrimitive(object.getItemNameFilters()).getPrimitive());
        }

        if (object.getItemLoreFilters() != null && !object.getItemLoreFilters().isEmpty()) {
            items.put("lore-filters", adapter.toPrimitive(object.getItemLoreFilters()).getPrimitive());
        }

        items.put("strip-colors", object.isStripColors());
        return ConfigPrimitive.ofMap(items);
    }
}
