package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Collections;

/**
 * A menu container that supports multiple pages of display.
 * <p>
 * This is achieved by making several pages of display for a menu.
 * </p>
 */
public abstract class PagedMenuContainer extends PlayerMenuContainer {
    private static final ItemStackBuilder DEFAULT_PAGE = new ItemStackBuilder()
            .material(XMaterial.OAK_SIGN)
            .name("&a{DIRECTION} Page");

    /** The parent config section containing the information of the paged menu */
    @Getter
    private final ConfigurationSection parentSection;
    /** The last known size of the menu */
    @Getter
    private int lastSize;

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
        this.lastSize = section.getInt("size");
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
     * Gets the maximum page that needs to be used for the given amount of elements.
     *
     * @param elements the amount of elements.
     * @return the max page.
     */
    public int getMaxPage(int elements) {
        return (int) Math.ceil(elements / (getLastSize() - 9F) - 1);
    }

    /**
     * Gets the size of the menu required for the given elements.
     * <p>
     * The resulting number includes the 9 slots at the bottom of the GUI for the menu buttons.
     * </p>
     *
     * @param elements the elements.
     * @return the size of the menu.
     */
    public int getMenuSize(int elements) {
        return Math.max((int) Math.ceil(elements / 9F) * 9 + 9, 54);
    }

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
                builder.specialPlaceholders("previous-page", () -> Collections.singleton(Placeholder.curly("DIRECTION", "Previous")));
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
                builder.specialPlaceholders("next-page", () -> Collections.singleton(Placeholder.curly("DIRECTION", "Next")));
                builder.bindTo("next-page", (event) -> {
                    setPage(page + 1);
                });
            }
        }

        builder.globalPlaceholders(Placeholder.curly("PAGE", this.page));
    }
}
