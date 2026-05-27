package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.registry.data.dialog.DialogBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A KCommon object adapter for dialog bases.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("UnstableApiUsage")
public class KDialogBase implements CASerializable, KDialogElement<DialogBase> {
    private Component title;
    private @Nullable Component externalTitle;
    private boolean canCloseWithEscape = true;
    private List<KDialogBody> dialogBody = new ArrayList<>();

    @Override
    public DialogBase toComponent() {
        return DialogBase.create(title, externalTitle, canCloseWithEscape, false, DialogBase.DialogAfterAction.CLOSE,
                dialogBody != null ? dialogBody.stream().map(KDialogBody::toComponent).toList() : Collections.emptyList(), Collections.emptyList());
    }
}
