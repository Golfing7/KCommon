package com.golfing8.kcommon.menu;

import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.util.Collection;
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
    private final MenuCoordinate slot;
    /**
     * A supplier of the special placeholders.
     */
    @Nullable @Getter @Setter
    private Supplier<Collection<Placeholder>> specialPlaceholders;
    /**
     * A supplier of the special multi-placeholders.
     */
    @Nullable @Getter @Setter
    private Supplier<Collection<MultiLinePlaceholder>> specialMPlaceholders;

    public SimpleGUIItem(ItemStackBuilder item, MenuCoordinate slot) {
        this.item = item;
        this.slot = slot;
    }

    public SimpleGUIItem(ConfigurationSection section) {
        if (section.contains("item")) {
            this.item = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "item"), ItemStackBuilder.class);
        } else {
            this.item = new ItemStackBuilder(section);
        }
        this.slot = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofSection(section).getSubValue("slot"), MenuCoordinate.class);
    }
}
