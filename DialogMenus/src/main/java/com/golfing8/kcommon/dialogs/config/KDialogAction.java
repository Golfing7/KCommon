package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter for working with {@link io.papermc.paper.registry.data.dialog.action.DialogAction} in KCommon.
 * <p>
 * Adds support for being configured.
 * </p>
 */
@SuppressWarnings("UnstableApiUsage")
public class KDialogAction implements CASerializable, KDialogElement<DialogAction> {
    private @Nullable String commandTemplate;

    @Override
    public DialogAction toComponent() {
        return null;
    }
}
