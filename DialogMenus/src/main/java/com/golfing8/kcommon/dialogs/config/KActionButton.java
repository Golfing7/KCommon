package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import io.papermc.paper.registry.data.dialog.ActionButton;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter for working with {@link io.papermc.paper.registry.data.dialog.ActionButton} in KCommon.
 * <p>
 * Adds support for being configured.
 * </p>
 */
@SuppressWarnings("UnstableApiUsage")
public class KActionButton implements CASerializable {
    private Component label;
    private @Nullable Component tooltip;
    private int width = 1;
    private @Nullable KDialogAction dialogAction;

    /**
     * Builds an action button instance from this KActionButton instance
     *
     * @return the built action button
     */
    public ActionButton toActionButton() {
        return ActionButton.create(label, tooltip, width, dialogAction != null ? dialogAction.toDialogAction() : null);
    }
}
