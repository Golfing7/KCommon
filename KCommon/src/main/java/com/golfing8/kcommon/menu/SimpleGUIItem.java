package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.struct.reflection.FieldType;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A class representing a simple GUI item. Intended to be loaded from a config.
 */
public class SimpleGUIItem {
    /**
     * The item to place into the GUI.
     */
    @Getter
    private final ItemStackBuilder item;
    /**
     * The menu coordinate to place the item at.
     */
    @Getter
    private final Set<MenuCoordinate> slots;

    @Deprecated
    public MenuCoordinate getSlot() {
        return slots.iterator().next();
    }

    /** The defining config section */
    @Getter @Nullable
    private ConfigurationSection configSection;

    /** The special material to use for building the item */
    @Getter @Setter
    private @Nullable Supplier<XMaterial> specialMaterial;

    /**
     * A supplier of the special placeholders.
     */
    @Nullable
    @Getter
    @Setter
    private Supplier<Collection<Placeholder>> specialPlaceholders;
    /**
     * A supplier of the special multi-placeholders.
     */
    @Nullable
    @Getter
    @Setter
    private Supplier<Collection<MultiLinePlaceholder>> specialMPlaceholders;

    public SimpleGUIItem(ItemStackBuilder item, MenuCoordinate slot) {
        this.item = item;
        this.slots = Sets.newHashSet(slot);
    }

    public SimpleGUIItem(ConfigurationSection section) {
        if (section.contains("item")) {
            this.item = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "item"), ItemStackBuilder.class);
        } else {
            this.item = new ItemStackBuilder(section);
        }
        if (section.contains("slots")) {
            this.slots = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofSection(section).getSubValue("slots"), FieldType.extractFrom(new TypeToken<Set<MenuCoordinate>>() {
            }));
        } else {
            this.slots = Sets.newHashSet(ConfigTypeRegistry.getFromType(ConfigPrimitive.ofSection(section).getSubValue("slot"), MenuCoordinate.class));
        }
        this.configSection = section;
    }

    /**
     * Builds the item from the template
     *
     * @return the item
     */
    public ItemStack buildFromTemplate() {
        return buildFromTemplate(null);
    }

    /**
     * Builds the item from the template
     *
     * @param player the player
     * @return the item
     */
    public ItemStack buildFromTemplate(@Nullable Player player) {
        if (specialMaterial != null) {
            XMaterial material = specialMaterial.get();
            if (material != null) {
                this.item.material(material);
            }
        }
        if (specialPlaceholders != null) {
            this.item.placeholders(specialPlaceholders.get());
        }
        if (specialMPlaceholders != null) {
            this.item.multiLinePlaceholders(specialMPlaceholders.get());
        }
        return this.item.buildFromTemplate(player);
    }
}
