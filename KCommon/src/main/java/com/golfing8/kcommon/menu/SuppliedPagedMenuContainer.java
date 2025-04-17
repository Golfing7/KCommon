package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.shape.LayoutShapeRectangle;
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
@Getter @Setter
public abstract class SuppliedPagedMenuContainer<T> extends PagedMenuContainer {
    /** The shape designated where we will store the entries for a page. */
    private @Nullable MenuLayoutShape elementSection;
    /** Sets ths source from where elements are pulled. */
    private @Nullable Function<Range, List<T>> elementSource;
    /** A supplier for the max number of elements that this menu has. */
    private @Nullable Supplier<Integer> elementCountSupplier;

    protected SuppliedPagedMenuContainer(ConfigurationSection section, Player player) {
        super(section, player);
    }

    protected SuppliedPagedMenuContainer(Player player) {
        super(player);
    }

    /**
     * Sets the parent section and overrides the element section if applicable.
     *
     * @param section the config section.
     */
    @Override
    public void setParentSection(@NotNull ConfigurationSection section) {
        super.setParentSection(section);

        MenuLayoutShape elementSection = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "element-section-shape"), MenuLayoutShape.class);
        if (elementSection != null) {
            this.elementSection = elementSection;
            setElementsPerPage(elementSection.getInRange().size());
        }
    }

    /**
     * Sets the supplier for element count.
     *
     * @param elementCountSupplier the element count supplier.
     */
    public void setElementCountSupplier(@NotNull Supplier<Integer> elementCountSupplier) {
        Preconditions.checkNotNull(elementCountSupplier, "elementCountSupplier cannot be null!");

        this.elementCountSupplier = elementCountSupplier;
        this.setMaxPageByElements(this.elementCountSupplier.get());
    }

    /**
     * sets the element source.
     *
     * @param elementSource the element source.
     */
    public void setElementSource(@NotNull Function<Range, List<T>> elementSource) {
        Preconditions.checkNotNull(elementSource, "elementSource cannot be null!");

        this.elementSource = elementSource;
    }

    /**
     * Sets the element section for this menu.
     * Note that if the menu is currently open and the number of elements per page, that
     * change will NOT be reflected.
     *
     * @param elementSection the element section.
     */
    public void setElementSection(@NotNull MenuLayoutShape elementSection) {
        Preconditions.checkNotNull(elementSection, "elementSection cannot be null!");

        this.elementSection = elementSection;
        setElementsPerPage(elementSection.getInRange().size());

        // Since elements per page can change, max page can also change.
        if (elementCountSupplier != null) {
            setMaxPageByElements(elementCountSupplier.get());
        }
    }

    /**
     * Updates the current page and re-opens the inventory for the player if necessary.
     *
     * @param page the page.
     */
    public void setPage(int page) {
        Preconditions.checkState(this.elementCountSupplier != null, "elementCountSupplier must not be null!");

        // Recalculate the max page if applicable.
        setMaxPageByElements(this.elementCountSupplier.get());
        page = Math.min(page, getMaxPage());
        super.setPage(page);
    }

    /**
     * Gets a range of all indices of elements on the current page.
     * Note that this range will respect {@link #elementCountSupplier}
     *
     * @return the range of indices, or null if menu has not been fully set up.
     */
    protected @Nullable Range getCurrentElementRange() {
        if (getElementsPerPage() <= 0)
            return null;

        Preconditions.checkState(this.elementCountSupplier != null, "elementCountSupplier must not be null!");
        int elementCount = elementCountSupplier.get();
        return new Range(getPage() * getElementsPerPage(), Math.min((getPage() + 1) * getElementsPerPage() - 1, elementCount - 1));
    }

    /**
     * Applies the given bi-consumer to every element on the current page of the menu.
     *
     * @param action the action to perform on the coordinates.
     */
    protected void forEachElementOnPage(@NotNull BiConsumer<MenuCoordinate, T> action) {
        Preconditions.checkNotNull(action, "Action cannot be null!");
        Preconditions.checkState(this.elementCountSupplier != null,  "elementCountSupplier must not be null!");
        Preconditions.checkState(this.elementSource != null,  "elementSource must not be null!");
        Preconditions.checkState(this.elementSection != null,  "elementSection must not be null!");

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
}
