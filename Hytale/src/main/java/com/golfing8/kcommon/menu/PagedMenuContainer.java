package com.golfing8.kcommon.menu;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A menu that shows a list of elements a page at a time, the Hytale counterpart of the Bukkit
 * {@code SuppliedPagedMenuContainer}. The menu's {@code elements-per-page} sets the page size.
 *
 * @param <T> the element type.
 */
public abstract class PagedMenuContainer<T> extends PlayerMenuContainer {
    @Getter
    private List<T> elements = Collections.emptyList();
    /** The current page, starting at 0. */
    @Getter
    private int page;
    private int maxPage;

    /**
     * Creates a container for the player.
     *
     * @param player the player.
     */
    protected PagedMenuContainer(PlayerRef player) {
        super(player);
    }

    /**
     * Sets the elements to page through.
     *
     * @param elements the elements.
     */
    public void setElements(List<T> elements) {
        this.elements = new ArrayList<>(elements);
    }

    /**
     * Gets the last page, starting at 0, as of the last build.
     *
     * @return the last page.
     */
    public int getMaxPage() {
        return maxPage;
    }

    /**
     * Moves to the page and rebuilds the menu. Pages past either end are clamped.
     *
     * @param page the page, starting at 0.
     */
    public void setPage(int page) {
        this.page = Math.max(0, Math.min(page, maxPage));
        refresh();
    }

    /**
     * Runs the action for every element on the current page, typically adding a row for each. Also adds the
     * {@code {PAGE}} and {@code {MAX_PAGE}} placeholders, counting from 1.
     *
     * @param builder the menu being built.
     * @param action  the action, given each element and its index in {@link #getElements()}.
     */
    protected void forEachElementOnPage(MenuBuilder builder, ElementConsumer<T> action) {
        int perPage = builder.getElementsPerPage() <= 0 ? Math.max(1, elements.size()) : builder.getElementsPerPage();
        maxPage = Math.max(0, (elements.size() - 1) / perPage);
        page = Math.max(0, Math.min(page, maxPage));
        builder.placeholders("PAGE", page + 1, "MAX_PAGE", maxPage + 1);

        int end = Math.min(elements.size(), (page + 1) * perPage);
        for (int i = page * perPage; i < end; i++) {
            action.accept(i, elements.get(i));
        }
    }

    /**
     * Binds the previous and next page buttons, disabling each at its end. Call after {@link #forEachElementOnPage}.
     *
     * @param builder  the menu being built.
     * @param previous the previous button's key.
     * @param next     the next button's key.
     * @return the builder.
     */
    protected MenuBuilder bindPageButtons(MenuBuilder builder, String previous, String next) {
        return builder.bindTo(previous, event -> setPage(page - 1))
                .disabled(previous, page <= 0)
                .bindTo(next, event -> setPage(page + 1))
                .disabled(next, page >= maxPage);
    }

    /**
     * Receives the elements of a page.
     *
     * @param <T> the element type.
     */
    @FunctionalInterface
    public interface ElementConsumer<T> {
        /**
         * Receives one element.
         *
         * @param index   the element's index in the whole list.
         * @param element the element.
         */
        void accept(int index, T element);
    }
}
