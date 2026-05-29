package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.set.RegistrySet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * A KCommon object representing an Adventure dialog.
 */
@SuppressWarnings("UnstableApiUsage")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KDialog implements CASerializable, KDialogElement<Dialog> {
    private @NotNull KDialogBase base = new KDialogBase(ComponentUtils.toComponent("KCommon Dialog"), null, true, Collections.emptyList());
    private @Nullable KActionButton notice;
    private @Nullable List<KDialog> dialogs;
    private @Nullable MultiAction multiAction;
    private @Nullable Confirmation confirmation;

    @Override
    public Dialog toComponent() {
        return Dialog.create(factory -> {
            var builder = factory.empty();
            builder.base(base.toComponent());
            if (notice != null) {
                builder.type(DialogType.notice(notice.toComponent()));
            } else if (dialogs != null) {
                builder.type(DialogType.dialogList(RegistrySet.keySetFromValues(RegistryKey.DIALOG, dialogs.stream().map(KDialog::toComponent).toList())).build());
            } else if (multiAction != null) {
                builder.type(DialogType.multiAction(multiAction.getActions().stream().map(KActionButton::toComponent).toList(),
                        multiAction.getExitButton() != null ? multiAction.getExitButton().toComponent() : null,
                        multiAction.getColumns()));
            } else if (confirmation != null) {
                builder.type(DialogType.confirmation(confirmation.getYes().toComponent(), confirmation.getNo().toComponent()));
            } else {
                throw new IllegalStateException("No dialog type selected!");
            }
        });
    }

    /**
     * A wrapper for a multi action dialog
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiAction implements CASerializable {
        private List<KActionButton> actions;
        private @Nullable KActionButton exitButton;
        private int columns = 1;
    }

    /**
     * A wrapper for a confirmation dialog
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Confirmation implements CASerializable {
        private KActionButton yes;
        private KActionButton no;
    }
}
