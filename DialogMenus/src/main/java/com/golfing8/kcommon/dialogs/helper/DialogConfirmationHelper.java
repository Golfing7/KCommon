package com.golfing8.kcommon.dialogs.helper;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.dialogs.config.KActionButton;
import com.golfing8.kcommon.dialogs.config.KDialog;
import com.golfing8.kcommon.dialogs.config.KDialogAction;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import io.papermc.paper.dialog.Dialog;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * A simple utility class for a player
 */
@UtilityClass
public class DialogConfirmationHelper {
    private static final Component DEFAULT_YES = ComponentUtils.toComponent("&aYes");
    private static final Component DEFAULT_NO = ComponentUtils.toComponent("&cNo");

    /**
     * Creates a confirmation promise for the player
     *
     * @param player the player
     * @return the promise of completion
     */
    public static Promise<Boolean> confirmation(Player player) {
        return confirmation(player, DEFAULT_YES, DEFAULT_NO);
    }

    /**
     * Creates a confirmation promise for the player
     *
     * @param player the player
     * @param yes the 'yes' title
     * @param no the 'no' title
     * @return the promise
     */
    public static Promise<Boolean> confirmation(Player player, Component yes, Component no) {
        Promise<Boolean> result = Promise.empty();
        KActionButton yesAction = new KActionButton(
                yes,
                null,
                256,
                new KDialogAction(null, null, (response, audience) -> result.supply(true))
        );

        KActionButton noAction = new KActionButton(
                no,
                null,
                256,
                new KDialogAction(null, null, (response, audience) -> result.supply(false))
        );

        KDialog kDialog = new KDialog(new NamespacedKey(KCommon.getInstance(), UUID.randomUUID().toString()), null, null, null, new KDialog.Confirmation(yesAction, noAction));
        Dialog dialog = kDialog.toComponent();
        player.showDialog(dialog);
        return result;
    }
}
