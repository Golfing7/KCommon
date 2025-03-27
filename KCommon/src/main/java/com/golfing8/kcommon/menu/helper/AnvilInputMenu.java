package com.golfing8.kcommon.menu.helper;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.NMSVersion;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.MenuShapeType;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.event.PrepareResultEvent;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareAnvilRepairEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

// Does not currently work. 1.21.1 will make this MUCH easier to manage.
public class AnvilInputMenu extends PlayerMenuContainer implements Listener {
    @Getter
    private final CompletableFuture<@Nullable String> result;
    private @Nullable String interimResult = null;
    private String inputName;

    public AnvilInputMenu(Player player, String inputName) {
        super(player);

        this.result = new CompletableFuture<>();
        this.inputName = inputName == null ? "&aInput" : inputName;
        Bukkit.getPluginManager().registerEvents(this, KCommon.getInstance());
        if (WineSpigot.isWineSpigot()) {
            // Register an event.
            Bukkit.getServer().getPluginManager().registerEvent(PrepareAnvilRepairEvent.class, this, EventPriority.HIGH, this::onAnvilRepairLegacy, KCommon.getInstance());
        }
    }

    public AnvilInputMenu(Player player) {
        this(player, null);
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder = MenuBuilder.builder()
                .shapeType(MenuShapeType.ANVIL)
                .title(inputName)
                .closeRunnable((event) -> {
                    HandlerList.unregisterAll(this);
                })
                .postCloseRunnable((event) -> {
                    result.complete(interimResult);
                });
        builder.setAt(0, XMaterial.EMERALD.parseItem());
        builder.addAction(2, (event) -> {
            if (KCommon.getInstance().getServerVersion().isAtOrAfter(NMSVersion.v1_21))
                interimResult = NMS.getTheNMS().getMagicInventories().getAnvilRepairName(event.getView());

            Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
                getPlayer().closeInventory();
            });
        });
        return builder.buildSimple();
    }

    public void onAnvilRepairLegacy(Listener listener, Event _event) {
        if (!(_event instanceof PrepareAnvilRepairEvent))
            return;

        PrepareAnvilRepairEvent event = (PrepareAnvilRepairEvent) _event;
        if (event.getResult() != null && event.getResult().hasItemMeta())
            interimResult = event.getResult().getItemMeta().getDisplayName();
    }
}
