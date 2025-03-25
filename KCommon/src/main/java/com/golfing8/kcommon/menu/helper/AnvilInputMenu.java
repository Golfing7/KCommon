package com.golfing8.kcommon.menu.helper;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.MenuShapeType;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareAnvilRepairEvent;
import org.bukkit.inventory.AnvilInventory;

import java.util.concurrent.CompletableFuture;

public class AnvilInputMenu extends PlayerMenuContainer {
    @Getter
    private final CompletableFuture<String> result;
    private String interimResult = null;

    public AnvilInputMenu(Player player) {
        super(player);

        this.result = new CompletableFuture<>();
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = MenuBuilder.builder()
                .shapeType(MenuShapeType.ANVIL)
                .closeRunnable((event) -> result.complete(interimResult));
        builder.setAt(0, XMaterial.EMERALD.parseItem());
        builder.addAction(2, (event) -> {
            result.complete(interimResult);
            Bukkit.getScheduler().runTaskLater(KCommon.getInstance(), () -> getPlayer().closeInventory(), 1);
        });
        return builder.buildSimple();
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        this.interimResult = NMS.getTheNMS().getMagicInventories().getAnvilRenameText(event.getView());
    }
}
