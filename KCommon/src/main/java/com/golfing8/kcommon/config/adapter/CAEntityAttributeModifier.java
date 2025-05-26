package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CAEntityAttributeModifier implements ConfigAdapter<EntityAttributeModifier> {
    private static final String DEFAULT_SERIALIZED_NAME = "KCommon Attribute Modifier";

    @Override
    public Class<EntityAttributeModifier> getAdaptType() {
        return EntityAttributeModifier.class;
    }

    @Override
    public @Nullable EntityAttributeModifier toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> objects = entry.unwrap();
        EntityAttributeModifier.Operation operation = EntityAttributeModifier.Operation.ADD_NUMBER;
        if (objects.containsKey("operation"))
            operation = EntityAttributeModifier.Operation.valueOf(objects.get("operation").toString());
        double amount = (double) ConfigPrimitive.coerceObjectToBoxed(objects.get("amount"), Double.class);
        String name = DEFAULT_SERIALIZED_NAME;
        if (objects.containsKey("name"))
            name = objects.get("name").toString();
        EquipmentSlot slot = null;
        if (objects.containsKey("slot"))
            slot = EquipmentSlot.valueOf(objects.get("slot").toString());
        return new EntityAttributeModifier(UUID.randomUUID(), name, amount, operation, slot);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull EntityAttributeModifier object) {
        Map<String, Object> values = new HashMap<>();
        values.put("operation", object.getOperation().name());
        if (!object.getName().equals(DEFAULT_SERIALIZED_NAME))
            values.put("name", object.getName());
        if (object.getSlot() != null)
            values.put("slot", object.getSlot().name());
        values.put("amount", object.getAmount());
        return ConfigPrimitive.ofMap(values);
    }
}
