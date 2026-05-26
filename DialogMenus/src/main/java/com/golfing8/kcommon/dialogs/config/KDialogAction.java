package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.event.ClickCallback;
import org.jetbrains.annotations.Nullable;

/**
 * An adapter for working with {@link io.papermc.paper.registry.data.dialog.action.DialogAction} in KCommon.
 * <p>
 * Adds support for being configured.
 * </p>
 */
@SuppressWarnings("UnstableApiUsage")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KDialogAction implements CASerializable, KDialogElement<DialogAction> {
    private @Nullable String commandTemplate;
    private @Nullable KClickEvent clickEvent;
    private transient @Nullable DialogActionCallback clickAction;

    @Override
    public DialogAction toComponent() {
        if (commandTemplate != null) {
            return DialogAction.commandTemplate(commandTemplate);
        } else if (clickEvent != null) {
            return DialogAction.staticAction(clickEvent.toComponent());
        } else if (clickAction != null) {
            return DialogAction.customClick(clickAction, ClickCallback.Options.builder().build());
        } else {
            throw new IllegalStateException("No selected action!");
        }
    }
}
