package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.InvalidConfigException;
import com.golfing8.kcommon.menu.MenuUtils;
import com.golfing8.kcommon.menu.movement.MoveLength;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"rawtypes"})
public class CAMoveLength implements ConfigAdapter<MoveLength> {
    @Override
    public Class<MoveLength> getAdaptType() {
        return MoveLength.class;
    }

    @Override
    public @Nullable MoveLength toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        if (entry.getPrimitive() instanceof String) {
            Range range = Range.fromString(entry.getPrimitive().toString());
            return MenuUtils.calculateMovement((int) range.getMin(), (int) range.getMax(), true);
        }

        if (entry.getPrimitive() instanceof List) {
            List items = (List) entry.getPrimitive();
            List<MenuCoordinate> menuCoordinates = new ArrayList<>();
            for (Object item : items) {
                MenuCoordinate menuCoordinate = ConfigTypeRegistry.getFromType(ConfigPrimitive.ofTrusted(item), MenuCoordinate.class);
                menuCoordinates.add(menuCoordinate);
            }
            return new MoveLength(menuCoordinates);
        }
        throw new InvalidConfigException("Could not interpret " + entry + " as MoveLength.");
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull MoveLength object) {
        List<Integer> slots = new ArrayList<>();
        for (MenuCoordinate menuCoordinate : object.getCoordinates()) {
            slots.add(MenuUtils.getSlotFromCartCoords(menuCoordinate.getX(), menuCoordinate.getY()));
        }
        return ConfigPrimitive.ofList(slots);
    }
}
