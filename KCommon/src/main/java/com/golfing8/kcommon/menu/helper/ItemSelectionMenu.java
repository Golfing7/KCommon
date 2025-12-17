package com.golfing8.kcommon.menu.helper;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuBuilder;
import com.golfing8.kcommon.menu.MenuShapeType;
import com.golfing8.kcommon.menu.PlayerMenuContainer;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.ItemUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * A menu for players to select an item from their own inventory
 */
public class ItemSelectionMenu extends PlayerMenuContainer {
    /** The promise of completion */
    @Getter
    private final Promise<@Nullable ItemStack> promise;
    private @Nullable ConfigurationSection menuSource;
    private boolean answering = false;

    public ItemSelectionMenu(Player player) {
        super(player);

        this.promise = Promise.empty();
    }

    public ItemSelectionMenu(Player player, @Nullable ConfigurationSection menuSource) {
        this(player);

        this.menuSource = menuSource;
    }

    @Override
    protected Menu loadMenu() {
        MenuBuilder builder;
        if (menuSource != null) {
            builder = new MenuBuilder(menuSource);
        } else {
            builder = MenuBuilder.builder()
                    .title("&aSelect an Item")
                    .shapeType(MenuShapeType.HOPPER);

            ItemStackBuilder item = new ItemStackBuilder()
                    .material(XMaterial.BLACK_STAINED_GLASS_PANE)
                    .name("&eSelect an item from your inventory");
            for (int i = 0; i < 5; i++) {
                builder.setAt(i, item.buildCached());
            }
        }
        builder.postCloseRunnable(event -> {
            if (answering)
                return;

            if (!promise.isDone())
                promise.supply(null);
        });
        builder.bottomClickAction(event -> {
            if (answering)
                return;

            if (!event.getClick().isLeftClick() && !event.getClick().isRightClick())
                return;

            ItemStack clicked = event.getCurrentItem();
            if (ItemUtil.isAirOrNull(clicked))
                return;

            answering = true;
            Bukkit.getScheduler().runTask(KCommon.getInstance(), () -> {
                event.getWhoClicked().closeInventory();
                promise.supply(clicked);
            });
        });

        return builder.buildSimple();
    }
}
