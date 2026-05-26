package com.golfing8.kcommon.dialogs.config;

import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.dialogs.KDialogElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.Nullable;

/**
 * A KCommon object representing a click event
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class KClickEvent implements CASerializable, KDialogElement<ClickEvent> {
    private @Nullable String url;
    private @Nullable String file;
    private @Nullable String clipboard;
    private @Nullable String runCommand;
    private @Nullable String suggestCommand;
    private int page = -1;

    @Override
    public ClickEvent toComponent() {
        if (page >= 0) {
            return ClickEvent.changePage(page);
        } else if (url != null) {
            return ClickEvent.openUrl(url);
        } else if (file != null) {
            return ClickEvent.openFile(file);
        } else if (clipboard != null) {
            return ClickEvent.copyToClipboard(clipboard);
        } else if (runCommand != null) {
            return ClickEvent.runCommand(runCommand);
        } else if (suggestCommand != null) {
            return ClickEvent.suggestCommand(suggestCommand);
        } else {
            throw new IllegalStateException("No selected action!");
        }
    }
}
