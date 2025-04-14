package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.menu.shape.MenuLayoutShape;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A menu container that supports multiple pages of display.
 * <p>
 * This is achieved by making several pages of display for a menu.
 * </p>
 */
@Getter
public abstract class PagedMenuContainer<T> extends PlayerMenuContainer {
    private static final ItemStackBuilder DEFAULT_PAGE = new ItemStackBuilder()
            .material(XMaterial.OAK_SIGN)
            .name("&a{DIRECTION} Page");

    /** The parent config section containing the information of the paged menu */
    private final ConfigurationSection parentSection;
    /** The shape designated where we will store the entries for a page. */
    @Setter
    private @Nullable MenuLayoutShape elementSection;
    /** Sets ths source from where elements are pulled. */
    private @Nullable Function<Range, List<T>> elementSource;
    /** A supplier for the max number of elements that this menu has. */
    private @Nullable Supplier<Integer> elementCountSupplier;
    /** The last known size of the menu */
    private int lastSize;
    /** The page index that is currently being displayed */
    private int page;
    /** The maximum page that can be set. By default, this is loaded from the config, but it is encouraged to override this value. */
    @Setter
    private int maxPage;
    /** How many elements should be expected to show per page. */
    @Setter
    private int elementsPerPage;

    public PagedMenuContainer(ConfigurationSection section, Player player) {
        super(player);

        this.parentSection = section;
        this.maxPage = section.getInt("max-page", 0);
        this.lastSize = section.getInt("size");
        this.elementSection = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "element-section-shape"), MenuLayoutShape.class);
        if (this.elementSection != null) {
            this.elementsPerPage = this.elementSection.getInRange().size();
        } else {
            this.elementsPerPage = lastSize - 9;
        }
    }

    @Override
    protected final Menu loadMenu() {
        MenuBuilder builder = new MenuBuilder(parentSection);
        if (builder.getMenuShapeType() != MenuShapeType.CHEST)
            throw new IllegalArgumentException("Shape type of paged menu MUST be CHEST!");

        this.lastSize = builder.getSize();
        adaptBuilder(builder);
        return loadMenu(builder);
    }

    /**
     * Loads the menu based off of the given menu builder.
     *
     * @param builder the builder.
     * @return the menu.
     */
    protected abstract Menu loadMenu(MenuBuilder builder);

    /**
     * Gets the max number of elements per page given the amount of elements shown on the page.
     *
     * @param elements the elements.
     * @return the max elements per page.
     */
    public int getMaxPage(int elements) {
        return (int) Math.ceil(elements / ((float) elementsPerPage) - 1);
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
     * Sets the max page of the GUI by the given amount of expected elements.
     *
     * @param elements the elements.
     */
    public void setMaxPageByElements(int elements) {
        this.setMaxPage(this.getMaxPage(elements));
    }

    /**
     * Updates the current page and re-opens the inventory for the player if necessary.
     *
     * @param page the page.
     */
    public void setPage(int page) {
        if (this.page == page)
            return;

        // Recalculate the max page if applicable.
        if (this.elementCountSupplier != null) {
            setMaxPageByElements(this.elementCountSupplier.get());
            page = Math.min(page, maxPage);
        }
        boolean reopen = getPlayer().getOpenInventory().getTopInventory() == menu.getGUI();
        this.page = page;
        this.menu = loadMenu();
        if (reopen) {
            open();
        }
    }

    /**
     * Gets a range of all indices of elements on the current page.
     * Note that this range will respect {@link #elementCountSupplier}
     *
     * @return the range of indices, or null if menu has not been fully set up.
     */
    protected @Nullable Range getCurrentElementRange() {
        if (elementsPerPage <= 0 || elementCountSupplier == null)
            return null;

        int elementCount = elementCountSupplier.get();
        return new Range(page * elementsPerPage, Math.min((page + 1) * elementsPerPage - 1, elementCount - 1));
    }

    /**
     * Gets the minimum slot view from the current page.
     *
     * @return the minimum slot view.
     * @deprecated access is too open and name is confusing
     */
    @Deprecated
    public int getMinimumSlotView() {
        if (lastSize < 0)
            return -1;

        return page * elementsPerPage;
    }

    /**
     * Gets the maximum slot view from the current page.
     *
     * @return the maximum slot view.
     * @deprecated access is too open and name is confusing
     */
    @Deprecated
    public int getMaximumSlotView() {
        if (lastSize < 0)
            return -1;

        return (page + 1) * elementsPerPage - 1;
    }

    /**
     * Sets the source of elements.
     *
     * @param elementSource the element source.
     * @param elementCount a supplier for the total number of elements.
     */
    protected void setElementSource(Function<Range, List<T>> elementSource, Supplier<Integer> elementCount) {
        this.elementSource = elementSource;
        this.elementCountSupplier = elementCount;
        this.setMaxPageByElements(elementCount.get());
    }

    /**
     * Applies the given bi-consumer to every element on the current page of the menu.
     */
    protected void forEachElementOnPage(@NotNull BiConsumer<MenuCoordinate, T> action) {
        Preconditions.checkNotNull(action, "Action cannot be null!");
        Preconditions.checkState(elementSource != null, "Element source has not been set!");
        Preconditions.checkState(elementCountSupplier != null, "Element count cannot be null!");
        Preconditions.checkState(elementSection != null, "Element section has not been set!");

        Range elementInterval = getCurrentElementRange();
        if (elementInterval == null)
            throw new IllegalStateException("Cannot loop over each element on when elementsPerPage has not been set!");

        // Get the coordinates and load the elements.
        List<MenuCoordinate> inRange = elementSection.getInRange();
        List<T> elements = elementSource.apply(elementInterval);
        int elementIndex = 0;
        for (int i = (int) elementInterval.getMin(); i <= elementInterval.getMax(); i++) {
            MenuCoordinate coordinate = inRange.get(elementIndex);
            action.accept(coordinate, elements.get(elementIndex++));
        }
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
