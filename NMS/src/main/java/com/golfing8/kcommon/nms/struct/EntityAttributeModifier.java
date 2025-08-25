package com.golfing8.kcommon.nms.struct;

import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
public class EntityAttributeModifier {
    private final UUID uuid;
    private final String name;
    private final double amount;
    private final Operation operation;
    private final @Nullable EquipmentSlot slot;

    public EntityAttributeModifier(@NotNull String name, double amount, @NotNull Operation operation) {
        this(UUID.randomUUID(), name, amount, operation);
    }

    public EntityAttributeModifier(@NotNull UUID uuid, @NotNull String name, double amount, @NotNull Operation operation) {
        this(uuid, name, amount, operation, null);
    }

    public EntityAttributeModifier(@NotNull UUID uuid, @NotNull String name, double amount, @NotNull Operation operation, @Nullable EquipmentSlot slot) {
        Validate.notNull(uuid, "UUID cannot be null");
        Validate.notNull(name, "Name cannot be null");
        Validate.notNull(operation, "Operation cannot be null");
        this.uuid = uuid;
        this.name = name;
        this.amount = amount;
        this.operation = operation;
        this.slot = slot;
    }

    public static enum Operation {
        ADD_NUMBER,
        ADD_SCALAR,
        MULTIPLY_SCALAR_1;

        private Operation() {
        }
    }
}
