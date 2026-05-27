package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

/**
 * A KCommon object adapter for dialog bodies
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class KDialogBody implements CASerializable, KDialogElement<DialogBody> {
    private @Nullable ItemStackBuilder item;
    private @Nullable Component message;
    private int messageWidth;
    private boolean decorations = false;
    private boolean tooltip = false;
    private int width = 64;
    private int height = 64;
    @Override
    public DialogBody toComponent() {
        if (item != null) {
            return DialogBody.item(item.buildFromTemplate(), message != null ? messageWidth > 0 ? DialogBody.plainMessage(message, messageWidth) : DialogBody.plainMessage(message) : null, decorations, tooltip, width, height);
        } else if (message != null) {
            return messageWidth > 0 ? DialogBody.plainMessage(message, messageWidth) : DialogBody.plainMessage(message);
        } else {
            throw new IllegalStateException("Item and message are both null!");
        }
    }
}
