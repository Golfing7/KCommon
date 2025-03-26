package com.golfing8.kcommon.menu.helper;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.MenuShapeType;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

/**
 * A simple confirmation menu.
 */
public class ConfirmMenu extends PlayerMenuContainer {
    public enum ConfirmationType {
        YES,
        NO,
        DIDNT_ANSWER,
        ;
    }

    @Getter
    private final CompletableFuture<ConfirmationType> result;

    public ConfirmMenu(Player player) {
        super(player);

        this.result = new CompletableFuture<>();
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = MenuBuilder.builder()
                .title("&aConfirm?")
                .shapeType(MenuShapeType.HOPPER)
                .postCloseRunnable((event) -> {
                    if (!result.isDone())
                        result.complete(ConfirmationType.DIDNT_ANSWER);
                });

        builder.setAt(0, new ItemStackBuilder().material(XMaterial.GREEN_STAINED_GLASS_PANE).name("&a✔").buildFromTemplate());
        builder.addAction(0, (event) -> {
            result.complete(ConfirmationType.YES);
            Bukkit.getScheduler().runTaskLater(KCommon.getInstance(), () -> getPlayer().closeInventory(), 1);
        });
        builder.setAt(4, new ItemStackBuilder().material(XMaterial.RED_STAINED_GLASS_PANE).name("&c❌").buildFromTemplate());
        builder.addAction(4, (event) -> {
            result.complete(ConfirmationType.NO);
            Bukkit.getScheduler().runTaskLater(KCommon.getInstance(), () -> getPlayer().closeInventory(), 1);
        });
        return builder.buildSimple();
    }
}
