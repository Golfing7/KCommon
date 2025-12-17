package com.golfing8.kcommon.menu.helper;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.MenuShapeType;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * A simple confirmation menu.
 */
public class ConfirmMenu extends PlayerMenuContainer {
    /**
     * The type of confirmation a player made
     */
    public enum ConfirmationType {
        YES,
        NO,
        DIDNT_ANSWER,
    }

    private final CompletableFuture<ConfirmationType> result;

    @Deprecated
    public CompletableFuture<ConfirmationType> getResult() {
        return result;
    }

    @Getter
    private final Promise<ConfirmationType> resultPromise;

    private boolean answering = false;

    public ConfirmMenu(Player player) {
        super(player);

        this.resultPromise = Promise.empty();
        this.result = this.resultPromise.toCompletableFuture();
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = MenuBuilder.builder()
                .title("&aConfirm?")
                .shapeType(MenuShapeType.HOPPER)
                .postCloseRunnable(event -> {
                    if (answering)
                        return;

                    if (!resultPromise.isDone())
                        resultPromise.supply(ConfirmationType.DIDNT_ANSWER);
                });

        builder.setAt(0, new ItemStackBuilder().material(XMaterial.GREEN_STAINED_GLASS_PANE).name("&a✔").buildFromTemplate());
        builder.addAction(0, event -> {
            answering = true;
            Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
                getPlayer().closeInventory();
                resultPromise.supply(ConfirmationType.YES);
            });
        });
        builder.setAt(4, new ItemStackBuilder().material(XMaterial.RED_STAINED_GLASS_PANE).name("&c❌").buildFromTemplate());
        builder.addAction(4, event -> {
            answering = true;
            Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
                getPlayer().closeInventory();
                resultPromise.supply(ConfirmationType.NO);
            });
        });
        return builder.buildSimple();
    }
}
