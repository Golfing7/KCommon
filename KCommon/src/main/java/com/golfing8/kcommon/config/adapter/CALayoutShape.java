package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.InvalidConfigException;
import com.golfing8.kcommon.menu.shape.LayoutShapeOutline;
import com.golfing8.kcommon.menu.shape.LayoutShapeRectangle;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.menu.shape.MenuLayoutShape;
import com.golfing8.kcommon.struct.Interval;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An adapter for layout shapes.
 */
public class CALayoutShape implements ConfigAdapter<MenuLayoutShape> {

    @Override
    public Class<MenuLayoutShape> getAdaptType() {
        return MenuLayoutShape.class;
    }

    @Override
    public @Nullable MenuLayoutShape toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> unwrapped = entry.unwrap();
        if (!unwrapped.containsKey("type"))
            throw new InvalidConfigException("Type is required for MenuLayoutShape");

        String layoutType = unwrapped.get("type").toString();
        if (layoutType.equalsIgnoreCase("RECTANGLE")) {
            MenuCoordinate slotLow = ConfigTypeRegistry.getFromType(entry.getSubValue("low-slot"), MenuCoordinate.class);
            MenuCoordinate slotHigh = ConfigTypeRegistry.getFromType(entry.getSubValue("high-slot"), MenuCoordinate.class);
            return new LayoutShapeRectangle(slotLow, slotHigh);
        } else if (layoutType.equalsIgnoreCase("OUTLINE")) {
            MenuCoordinate slotLow = ConfigTypeRegistry.getFromType(entry.getSubValue("low-slot"), MenuCoordinate.class);
            MenuCoordinate slotHigh = ConfigTypeRegistry.getFromType(entry.getSubValue("high-slot"), MenuCoordinate.class);
            return new LayoutShapeOutline(slotLow, slotHigh);
        } else {
            throw new InvalidConfigException("Unsupported Layout Type: " + layoutType);
        }
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull MenuLayoutShape object) {
        Map<String, Object> data = new HashMap<>();
        int minimumSlot = Integer.MAX_VALUE;
        int maximumSlot = Integer.MIN_VALUE;
        for (MenuCoordinate coordinate : object.getInRange()) {
            if (coordinate.toSlot() < minimumSlot)
                minimumSlot = coordinate.toSlot();
            if (coordinate.toSlot() > maximumSlot)
                maximumSlot = coordinate.toSlot();
        }
        data.put("low-slot", minimumSlot);
        data.put("high-slot", maximumSlot);
        if (object instanceof LayoutShapeRectangle) {
            data.put("type", "RECTANGLE");
            return ConfigPrimitive.ofMap(data);
        } else if (object instanceof LayoutShapeOutline) {
            data.put("type", "OUTLINE");
            return ConfigPrimitive.ofMap(data);
        } else {
            throw new InvalidConfigException("Unsupported Layout Shape " + object.getClass().getName());
        }
    }
}
