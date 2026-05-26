package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.set.RegistrySet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * A KCommon object representing an Adventure dialog.
 */
@SuppressWarnings("UnstableApiUsage")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class KDialog implements CASerializable, KDialogElement<Dialog> {
    private NamespacedKey key;
    private @Nullable KActionButton notice;
    private @Nullable List<KDialog> dialogs;
    private @Nullable MultiAction multiAction;
    private @Nullable Confirmation confirmation;

    @Override
    public void onDeserialize() {
        if (key == null) {
            key = new NamespacedKey(KCommon.getInstance(), UUID.randomUUID().toString());
        }
    }

    @Override
    public Dialog toComponent() {
        return Dialog.create(factory -> {
            var builder = factory.copyFrom(DialogKeys.create(key));
            if (notice != null) {
                builder.type(DialogType.notice(notice.toComponent()));
            } else if (dialogs != null) {
                builder.type(DialogType.dialogList(RegistrySet.keySetFromValues(RegistryKey.DIALOG, dialogs.stream().map(KDialog::toComponent).toList())).build());
            } else if (multiAction != null) {
                builder.type(DialogType.multiAction(multiAction.getActions().stream().map(KActionButton::toComponent).toList(),
                        multiAction.getExitButton().toComponent(), multiAction.getColumns()));
            } else if (confirmation != null) {
                builder.type(DialogType.confirmation(confirmation.getYes().toComponent(), confirmation.getNo().toComponent()));
            } else {
                throw new IllegalStateException("No dialog type selected!");
            }
        });
    }

    @Getter
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiAction implements CASerializable {
        private List<KActionButton> actions;
        private @Nullable KActionButton exitButton;
        private int columns = 1;
    }

    @Getter
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Confirmation implements CASerializable {
        private KActionButton yes;
        private KActionButton no;
    }
}
