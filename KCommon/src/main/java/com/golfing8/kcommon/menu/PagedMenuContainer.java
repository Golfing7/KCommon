package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * A menu container that supports multiple pages of display.
 * <p>
 * This is achieved by making several pages of display for a menu.
 * </p>
 */
public abstract class PagedMenuContainer extends PlayerMenuContainer {
    private static final ItemStackBuilder DEFAULT_PAGE = new ItemStackBuilder()
            .material(XMaterial.OAK_SIGN)
            .name("{DIRECTION} Page");

    /** The parent config section containing the information of the paged menu */
    @Getter
    private final ConfigurationSection parentSection;
    /** The last known size of the menu */
    private int lastSize = -1;

    /** The page that is currently being displayed */
    @Getter
    private int page;

    /** The maximum page that can be set. By default, this is loaded from the config, but it is encouraged to override this value. */
    @Getter @Setter
    private int maxPage;

    public PagedMenuContainer(ConfigurationSection section, Player player) {
        super(player);

        this.parentSection = section;
        this.maxPage = section.getInt("max-page", 0);
    }

    @Override
    protected final Menu loadMenu() {
        MenuBuilder builder = new MenuBuilder(parentSection);
        if (builder.getMenuShapeType() != MenuShapeType.CHEST)
            throw new IllegalArgumentException("Shape type of paged menu MUST be CHEST!");

        this.lastSize = builder.getSize();
        adaptBuilder(builder);
        this.menu = loadMenu(builder);
        return menu;
    }

    /**
     * Loads the menu based off of the given menu builder.
     *
     * @param builder the builder.
     * @return the menu.
     */
    protected abstract Menu loadMenu(MenuBuilder builder);

    /**
     * Updates the current page and re-opens the inventory for the player if necessary.
     *
     * @param page the page.
     */
    public void setPage(int page) {
        if (this.page == page)
            return;

        boolean reopen = getPlayer().getOpenInventory().getTopInventory() == menu.getGUI();
        this.page = page;
        this.menu = loadMenu();
        if (reopen) {
            open();
        }
    }

    /**
     * Gets the minimum slot view from the current page.
     *
     * @return the minimum slot view.
     */
    public int getMinimumSlotView() {
        if (lastSize < 0)
            return -1;

        return page * (lastSize - 9);
    }

    /**
     * Gets the maximum slot view from the current page.
     *
     * @return the maximum slot view.
     */
    public int getMaximumSlotView() {
        if (lastSize < 0)
            return -1;

        return (page + 1) * (lastSize - 9) - 1;
    }

    /**
     * Adapts the builder to THIS container.
     *
     * @param builder the builder.
     */
    protected void adaptBuilder(MenuBuilder builder) {
        if (page > 0) {
            if (builder.getSpecialItem("previous-page") == null) {
                builder.setSpecialItem("previous-page", new SimpleGUIItem(
                        DEFAULT_PAGE.addPlaceholders(Placeholder.curly("DIRECTION", "Previous")),
                        MenuUtils.getCartCoordsFromSlot(builder.getSize() - 9))
                );
                builder.bindTo("previous-page", (event) -> {
                    setPage(page - 1);
                });
            }
        }

        if (page < maxPage) {
            if (builder.getSpecialItem("next-page") == null) {
                builder.setSpecialItem("next-page", new SimpleGUIItem(
                        DEFAULT_PAGE.addPlaceholders(Placeholder.curly("DIRECTION", "Next")),
                        MenuUtils.getCartCoordsFromSlot(builder.getSize() - 1))
                );
                builder.bindTo("next-page", (event) -> {
                    setPage(page + 1);
                });
            }
        }

        builder.globalPlaceholders(Placeholder.curly("PAGE", this.page));
    }
}
