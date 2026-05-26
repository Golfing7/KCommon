package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.registry.data.dialog.ActionButton;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter for working with {@link io.papermc.paper.registry.data.dialog.ActionButton} in KCommon.
 * <p>
 * Adds support for being configured.
 * </p>
 */
@SuppressWarnings("UnstableApiUsage")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KActionButton implements CASerializable, KDialogElement<ActionButton> {
    private Component label;
    private @Nullable Component tooltip;
    private int width = 1;
    private @Nullable KDialogAction dialogAction;

    @Override
    public ActionButton toComponent() {
        return ActionButton.create(label, tooltip, width, dialogAction != null ? dialogAction.toComponent() : null);
    }
}
