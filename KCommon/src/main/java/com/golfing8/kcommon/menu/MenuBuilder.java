package com.golfing8.kcommon.menu;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.ClickRunnable;
import com.golfing8.kcommon.menu.action.CloseRunnable;
import com.golfing8.kcommon.menu.shape.LayoutShapeRectangle;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.menu.shape.MenuLayoutShape;
import com.golfing8.kcommon.struct.ChancedReference;
import com.golfing8.kcommon.struct.Pair;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * A builder class for {@link Menu}.
 * <p>
 * Contains utilities for building a menu from a config source as well
 * as manually building one from pure code.
 * </p>
 */
public final class MenuBuilder {
    private static final ItemStackBuilder DEFAULT_FILLER = new ItemStackBuilder()
            .material(XMaterial.GRAY_STAINED_GLASS_PANE)
            .name("&7");

    @Getter
    private int size = 27;
    private @Nullable Player placeholderTarget;
    private boolean canExpire = true, clickable;
    @Getter
    private String title = "&cKCommon GUI";
    private Map<Integer, List<ClickAction>> clickActions = new LinkedHashMap<>();
    private List<Pair<MenuLayoutShape, List<ChancedReference<ItemStack>>>> shapeCreation = new ArrayList<>();
    private Map<Integer, ItemStack> specificItems = new LinkedHashMap<>();
    /**
     * The bottom click action, run when any slot in the bottom inventory is clicked.
     */
    private ClickAction bottomClickEvent = null;
    /**
     * The top click event is called when a player clicks the top menu. Regardless if there is a click action on the slot clicked
     */
    private ClickAction topClickEvent = null;
    /**
     * Slots in the menu that are locked from interaction.
     * Items cannot be placed in them and items cannot be taken from them.
     */
    private Set<Integer> lockedSlots = new HashSet<>();
    /**
     * A map containing all special GUI items, mapped from their keys.
     */
    @Getter
    private Map<String, SimpleGUIItem> specialGUIItems = new LinkedHashMap<>();
    /**
     * A map containing string key bindings to specific items in this GUI.
     * Will be applied to the same slot defined for the 'special gui items' or left out if they are not present.
     */
    private Map<String, ClickAction> specialBindings = new LinkedHashMap<>();
    /**
     * A map containing placeholders for each special item.
     */
    private Map<String, Supplier<Collection<Placeholder>>> specialPlaceholders = new LinkedHashMap<>();
    /**
     * A map containing multiline placeholders for each special item.
     */
    private Map<String, Supplier<Collection<MultiLinePlaceholder>>> specialMPlaceholders = new LinkedHashMap<>();
    /**
     * The global placeholders to apply to EVERY string in this menu.
     */
    @Getter
    private List<Placeholder> globalPlaceholders = new ArrayList<>();
    /**
     * The global multiline placeholders for this menu
     */
    @Getter
    private List<MultiLinePlaceholder> globalMultiLinePlaceholders = new ArrayList<>();
    /**
     * The other GUI items to apply in this menu.
     */
    private Map<String, SimpleGUIItem> otherGUIItems = new LinkedHashMap<>();
    /**
     * The type of menu being built
     */
    @Getter
    private MenuShapeType menuShapeType = MenuShapeType.CHEST;
    /**
     * This will be run in the same tick the inventory has been closed
     */
    @Getter
    private CloseRunnable closeRunnable;
    /**
     * This will be run at the end of the tick the inventory has been closed. Useful for opening another menu
     */
    @Getter
    private CloseRunnable postCloseRunnable;
    /**
     * The tick runnable
     */
    @Getter
    private Runnable tickRunnable;

    private MenuBuilder() {
    }

    public MenuBuilder(MenuBuilder other) {
        this.size = other.size;
        this.canExpire = other.canExpire;
        this.clickable = other.clickable;
        this.title = other.title;
        this.clickActions = new HashMap<>(other.clickActions);
        this.shapeCreation = new ArrayList<>(other.shapeCreation);
        this.specificItems = new HashMap<>(other.specificItems);
        this.bottomClickEvent = other.bottomClickEvent;
        this.topClickEvent = other.topClickEvent;
        this.specialGUIItems = new HashMap<>(other.specialGUIItems);
        this.specialBindings = new HashMap<>(other.specialBindings);
        this.specialPlaceholders = new HashMap<>(other.specialPlaceholders);
        this.specialMPlaceholders = new HashMap<>(other.specialMPlaceholders);
        this.globalPlaceholders = new ArrayList<>(other.globalPlaceholders);
        this.globalMultiLinePlaceholders = new ArrayList<>(other.globalMultiLinePlaceholders);
        this.otherGUIItems = new HashMap<>(other.otherGUIItems);
        this.menuShapeType = other.menuShapeType;
        this.closeRunnable = other.closeRunnable;
        this.postCloseRunnable = other.postCloseRunnable;
        this.tickRunnable = other.tickRunnable;
        this.placeholderTarget = other.placeholderTarget;
        this.lockedSlots = new HashSet<>(other.lockedSlots);
    }

    /**
     * Creates a menu builder from the given configuration section.
     */
    public MenuBuilder(ConfigurationSection section) {
        if (section.contains("type")) {
            this.menuShapeType = MenuShapeType.valueOf(section.getString("type").toUpperCase());
            if (section.contains("size"))
                this.size = section.getInt("size");
            else
                this.size = this.menuShapeType.getType().getDefaultSize();
        } else if (section.contains("size")) {
            this.menuShapeType = MenuShapeType.CHEST;
            this.size = section.getInt("size");
        } else {
            this.menuShapeType = MenuShapeType.CHEST;
            this.size = 27;
        }

        this.title = section.getString("title");

        // Checks if either
        // 1: You've set 'use-filler-item' to true
        // 2: Have specified a filler-item and have not EXPLICITLY set use-filler-item
        if (section.getBoolean("use-filler-item") ||
                section.contains("filler-item") && !section.contains("use-filler-item")) {
            ItemStackBuilder fillerItem = section.contains("filler-item") ?
                    new ItemStackBuilder(section.getConfigurationSection("filler-item")) :
                    DEFAULT_FILLER;
            if (section.contains("filler-shape")) {
                MenuLayoutShape shape = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "filler-shape"), MenuLayoutShape.class);
                this.drawShape(shape, fillerItem.buildFromTemplate());
            } else {
                this.filler(fillerItem.buildFromTemplate());
            }
        }

        //Check for the special items.
        if (section.contains("special-slots")) {
            ConfigurationSection specialSection = section.getConfigurationSection("special-slots");
            for (String specialKey : specialSection.getKeys(false)) {
                SimpleGUIItem guiItem = new SimpleGUIItem(specialSection.getConfigurationSection(specialKey));
                this.specialGUIItems.put(specialKey, guiItem);
            }
        }

        //Check for other items.
        if (section.contains("other-slots")) {
            ConfigurationSection otherSection = section.getConfigurationSection("other-slots");
            for (String otherKey : otherSection.getKeys(false)) {
                SimpleGUIItem guiItem = new SimpleGUIItem(otherSection.getConfigurationSection(otherKey));
                this.otherGUIItems.put(otherKey, guiItem);
            }
        }
    }

    /**
     * Creates an empty builder instance
     *
     * @return the new builder instance
     */
    public static MenuBuilder builder() {
        return new MenuBuilder();
    }

    /**
     * Sets an 'other' item under the given key to the given item
     *
     * @param key the key
     * @param item the item
     */
    public void setOtherItem(String key, SimpleGUIItem item) {
        this.otherGUIItems.put(key, item);
    }

    /**
     * Gets the 'other' item under the given key
     *
     * @param key the key
     * @return the item
     */
    public SimpleGUIItem getOtherItem(String key) {
        return this.otherGUIItems.get(key);
    }

    /**
     * Sets the special item with the given key.
     *
     * @param key  the key.
     * @param item the item.
     */
    public void setSpecialItem(String key, SimpleGUIItem item) {
        this.specialGUIItems.put(key, item);
    }

    /**
     * Gets the 'special' item under the given key
     *
     * @param key the key
     * @return the item
     */
    public SimpleGUIItem getSpecialItem(String key) {
        return specialGUIItems.get(key);
    }

    /**
     * Sets the placeholder target for this menu. If non-null, PlaceholderAPI placeholders
     * will be parsed on the given target
     *
     * @param placeholderTarget the target
     * @return this
     */
    public MenuBuilder placeholderTarget(@Nullable Player placeholderTarget) {
        this.placeholderTarget = placeholderTarget;
        return this;
    }

    /**
     * Sets the tick runnable action for this menu.
     * This will be run once per game tick.
     *
     * @param runnable the runnable
     * @return this
     */
    public MenuBuilder tickRunnable(Runnable runnable) {
        this.tickRunnable = runnable;
        return this;
    }

    /**
     * Sets the action to be run while this menu is being closed
     *
     * @param closeRunnable the action
     * @return this
     */
    public MenuBuilder closeRunnable(CloseRunnable closeRunnable) {
        this.closeRunnable = closeRunnable;
        return this;
    }

    /**
     * Sets the action to be run after this menu has been closed
     *
     * @param postCloseRunnable the action
     * @return this
     */
    public MenuBuilder postCloseRunnable(CloseRunnable postCloseRunnable) {
        this.postCloseRunnable = postCloseRunnable;
        return this;
    }

    /**
     * Sets the 'locked slots' for the menu.
     * <p>
     * A locked slot is a slot in which no item interactions can take place
     * </p>
     *
     * @param slots the slots
     * @return this
     */
    public MenuBuilder lockedSlots(int... slots) {
        this.lockedSlots.clear();
        return addLockedSlots(slots);
    }

    /**
     * Adds locked slots to the menu
     *
     * @param slots the slots
     * @return this
     */
    public MenuBuilder addLockedSlots(int... slots) {
        for (int i : slots) {
            this.lockedSlots.add(i);
        }
        return this;
    }

    /**
     * Sets global multiline placeholders that will be parsed on this menu
     * and the items within it
     *
     * @param placeholders the placeholders
     * @return this
     */
    public MenuBuilder globalMultiLinePlaceholders(MultiLinePlaceholder... placeholders) {
        this.globalMultiLinePlaceholders = Arrays.asList(placeholders);
        return this;
    }

    /**
     * Sets the action to be called when any click occurs in the top section of this menu
     *
     * @param runnable the action
     * @return this
     */
    public MenuBuilder topClickAction(ClickRunnable runnable) {
        this.topClickEvent = new ClickAction(runnable);
        return this;
    }

    /**
     * Sets the global placeholders to be parsed on this menu and all items within it
     *
     * @param placeholders the placeholders
     * @return this
     */
    public MenuBuilder globalPlaceholders(Placeholder... placeholders) {
        this.globalPlaceholders = new ArrayList<>(Arrays.asList(placeholders));
        return this;
    }

    /**
     * Adds global placeholders to be parsed on this menu and everything in it
     *
     * @param placeholders the placeholders
     * @return this
     */
    public MenuBuilder addGlobalPlaceholders(Placeholder... placeholders) {
        if (this.globalPlaceholders == null || this.globalPlaceholders.isEmpty()) {
            this.globalPlaceholders(placeholders);
        } else {
            this.globalPlaceholders.addAll(Arrays.asList(placeholders));
        }
        return this;
    }

    /**
     * Sets a supplier for multiline placeholders tied to a special item
     *
     * @param special the special item to bind to
     * @param placeholders the multiline placeholders
     * @return this
     */
    public MenuBuilder specialMPlaceholders(String special, Supplier<Collection<MultiLinePlaceholder>> placeholders) {
        this.specialMPlaceholders.put(special, placeholders);
        return this;
    }

    /**
     * Sets the multiline placeholders ties to a special item
     *
     * @param special the special item to bind to
     * @param placeholders the multiline placeholders
     * @return this
     */
    public MenuBuilder specialMPlaceholders(String special, Collection<MultiLinePlaceholder> placeholders) {
        this.specialMPlaceholders.put(special, () -> placeholders);
        return this;
    }

    /**
     * Sets a supplier for placeholders tied to a special item
     *
     * @param special the special item to bind to
     * @param placeholders the placeholders
     * @return this
     */
    public MenuBuilder specialPlaceholders(String special, Supplier<Collection<Placeholder>> placeholders) {
        this.specialPlaceholders.put(special, placeholders);
        return this;
    }

    /**
     * Sets placeholders tied to a special item
     *
     * @param special the special item to bind to
     * @param placeholders the placeholders
     * @return this
     */
    public MenuBuilder specialPlaceholders(String special, Collection<Placeholder> placeholders) {
        this.specialPlaceholders.put(special, () -> placeholders);
        return this;
    }

    /**
     * Binds a {@link ClickRunnable} to a given special item.
     *
     * @param special the special item
     * @param runnable the action
     * @return this
     */
    public MenuBuilder bindTo(String special, ClickRunnable runnable) {
        this.specialBindings.put(special, new ClickAction(runnable));
        return this;
    }

    /**
     * Binds a {@link ClickAction} to a given special item
     *
     * @param special the special item
     * @param action the action
     * @return this
     */
    public MenuBuilder bindTo(String special, ClickAction action) {
        this.specialBindings.put(special, action);
        return this;
    }

    /**
     * Sets the shape type of this menu
     *
     * @param type the type
     * @return this
     */
    public MenuBuilder shapeType(MenuShapeType type) {
        Preconditions.checkNotNull(type, "Type cannot be null");
        this.menuShapeType = type;
        return this;
    }

    /**
     * Sets the size of this menu
     * <p>
     * The size must be divisible by 9 to fit Minecraft's inventory limitations
     * </p>
     *
     * @param size the size
     * @return this
     */
    public MenuBuilder size(int size) {
        this.size = size;
        return this;
    }

    /**
     * Sets if this menu is normally interactable.
     * <p>
     * This means that items can be taken out and deposited into this menu
     * </p>
     *
     * @param clickable true if clickable
     * @return this
     */
    public MenuBuilder clickable(boolean clickable) {
        this.clickable = clickable;
        return this;
    }

    /**
     * Sets the title of this menu
     *
     * @param title the title
     * @return this
     */
    public MenuBuilder title(String title) {
        this.title = title;
        return this;
    }

    /**
     * Sets if this menu can naturally expire, i.e. be garbage collected
     *
     * @param canExpire true
     * @return this
     */
    public MenuBuilder expire(boolean canExpire) {
        this.canExpire = canExpire;
        return this;
    }

    /**
     * Sets the item at the given slot
     *
     * @param slot the slot
     * @param itemStack the item
     * @return this
     */
    public MenuBuilder setAt(int slot, ItemStack itemStack) {
        this.specificItems.put(slot, itemStack);
        return this;
    }

    /**
     * Sets the item at the given x, y coordinate of the menu
     *
     * @param x the x
     * @param y the y
     * @param itemStack the item
     * @return this
     */
    public MenuBuilder setAt(int x, int y, ItemStack itemStack) {
        this.specificItems.put(MenuUtils.getSlotFromCartCoords(menuShapeType, x, y), itemStack);
        return this;
    }

    /**
     * Sets the filler item of the menu
     *
     * @param filler the filler
     * @return this
     */
    public MenuBuilder filler(ItemStack filler) {
        return this.filler(Lists.newArrayList(new ChancedReference<>(filler)));
    }

    /**
     * Sets a chanced reference for the filler item of the menu
     *
     * @param filler the filler
     * @return this
     */
    public MenuBuilder filler(ChancedReference<ItemStack> filler) {
        return this.filler(Lists.newArrayList(filler));
    }

    /**
     * Sets a list of chanced items for the filler of this menu
     *
     * @param filler the filler of the menu
     * @return this
     */
    public MenuBuilder filler(List<ChancedReference<ItemStack>> filler) {
        MenuCoordinate cartCoordsFromSlot = MenuUtils.getCartCoordsFromSlot(menuShapeType, size - 1);
        LayoutShapeRectangle key = new LayoutShapeRectangle(new MenuCoordinate(1, 1), cartCoordsFromSlot);
        return this.drawShape(key, filler);
    }

    /**
     * Draws a shape with the given item in this menu
     *
     * @param shape the shape
     * @param stack the item
     * @return this
     */
    public MenuBuilder drawShape(MenuLayoutShape shape, ItemStack stack) {
        return this.drawShape(shape, new ChancedReference<>(stack));
    }

    /**
     * Draws a shape with the given item and a chanced reference
     *
     * @param shape the shape
     * @param reference the item
     * @return this
     */
    public MenuBuilder drawShape(MenuLayoutShape shape, ChancedReference<ItemStack> reference) {
        return this.drawShape(shape, Lists.newArrayList(reference));
    }

    /**
     * Draws a shape with the given item and a list of chanced item references
     *
     * @param shape the shape
     * @param reference the items
     * @return this
     */
    public MenuBuilder drawShape(MenuLayoutShape shape, List<ChancedReference<ItemStack>> reference) {
        this.shapeCreation.add(new Pair<>(shape, reference));
        return this;
    }

    /**
     * Adds a click action to the given x, y coordinate pair
     *
     * @param x the x
     * @param y the y
     * @param action the action
     * @return this
     */
    public MenuBuilder addAction(int x, int y, ClickAction action) {
        return this.addAction(MenuUtils.getSlotFromCartCoords(menuShapeType, x, y), action);
    }

    /**
     * Adds a click action to the given x, y coordinate pair
     *
     * @param x the x
     * @param y the y
     * @param runnable the action
     * @return this
     */
    public MenuBuilder addAction(int x, int y, ClickRunnable runnable) {
        return this.addAction(MenuUtils.getSlotFromCartCoords(menuShapeType, x, y), runnable);
    }

    /**
     * Adds a click action to the given slot
     *
     * @param slot the slot
     * @param runnable the action
     * @return this
     */
    public MenuBuilder addAction(int slot, ClickRunnable runnable) {
        return this.addAction(slot, new ClickAction(200L, runnable));
    }

    /**
     * Adds a click action to the given slot
     *
     * @param slot the slot
     * @param action the action
     * @return this
     */
    public MenuBuilder addAction(int slot, ClickAction action) {
        if (clickActions.containsKey(slot)) {
            this.clickActions.get(slot).add(action);
        } else {
            this.clickActions.put(slot, Lists.newArrayList(action));
        }
        return this;
    }

    /**
     * Binds an action for all clicks made in the bottom section of the player's inventory view
     * when this menu is open
     *
     * @param runnable the action
     * @return this
     */
    public MenuBuilder bottomClickAction(ClickRunnable runnable) {
        this.bottomClickEvent = runnable == null ? null : new ClickAction(runnable);
        return this;
    }

    private void applyToMenu(MenuAbstract menu) {
        ItemStack[] contents = new ItemStack[size];

        for (Pair<MenuLayoutShape, List<ChancedReference<ItemStack>>> shapeListPair : shapeCreation) {
            for (MenuCoordinate coordinate : shapeListPair.getA().getInRange()) {
                int slot = MenuUtils.getSlotFromCartCoords(this.menuShapeType, coordinate.getX(), coordinate.getY());

                ItemStack found = null;
                while (found == null) {
                    ChancedReference<ItemStack> itemStackChancedReference = shapeListPair.getB().get(ThreadLocalRandom.current().nextInt(shapeListPair.getB().size()));
                    if (itemStackChancedReference.testRandom()) {
                        found = itemStackChancedReference.getReference();
                    }
                }

                contents[slot] = found;
            }
        }

        menu.setContents(contents);
        menu.setTopClickAction(this.topClickEvent);
        menu.setTickRunnable(this.tickRunnable);
        menu.setLockedSlots(this.lockedSlots);
        menu.onClose(this.closeRunnable);
        menu.onPostClose(this.postCloseRunnable);
        menu.setBottomClickAction(this.bottomClickEvent);

        otherGUIItems.forEach((key, item) -> {
            item.getSlots().forEach(coordinate -> {
                menu.setItemAt(coordinate.getX(), coordinate.getY(), item.getItem().buildFromTemplate(placeholderTarget));
            });
        });

        //Attempt to create the special bindings.
        for (Map.Entry<String, ClickAction> specialBinding : this.specialBindings.entrySet()) {
            String key = specialBinding.getKey();
            SimpleGUIItem guiItem = this.specialGUIItems.get(key);
            if (guiItem == null)
                continue;

            //Calculate the slots and add the actions and item.
            Supplier<Collection<Placeholder>> placeholders =
                    this.specialPlaceholders.getOrDefault(key, Collections::emptyList);
            Supplier<Collection<MultiLinePlaceholder>> mPlaceholders =
                    this.specialMPlaceholders.getOrDefault(key, Collections::emptyList);
            guiItem.setSpecialPlaceholders(placeholders);
            guiItem.setSpecialMPlaceholders(mPlaceholders);
            guiItem.getItem().placeholders(placeholders.get().toArray(new Placeholder[0]));
            guiItem.getItem().multiLinePlaceholders(mPlaceholders.get().toArray(new MultiLinePlaceholder[0]));

            //Add the item to the GUI.
            guiItem.getSlots().forEach(coordinate -> {
                int slot = MenuUtils.getSlotFromCartCoords(menuShapeType, coordinate.getX(), coordinate.getY());
                this.addAction(slot, specialBinding.getValue());
                this.setAt(slot,
                        guiItem.getItem().buildFromTemplate(placeholderTarget));
            });
            menu.addSpecialItem(key, guiItem);
        }

        for (Map.Entry<Integer, ItemStack> items : specificItems.entrySet()) {
            menu.setItemAt(items.getKey(), items.getValue());
        }
    }

    /**
     * Builds a {@link MenuSimple} instance with this builder
     *
     * @return a new {@link MenuSimple} instance
     */
    public MenuSimple buildSimple() {
        MenuSimple menuSimple = new MenuSimple(this.title, new MenuShape(menuShapeType, size), clickable, canExpire, clickActions, this.globalPlaceholders, this.globalMultiLinePlaceholders);
        applyToMenu(menuSimple);
        return menuSimple;
    }

    /**
     * Builds a {@link MenuDynamic} instance with this builder
     *
     * @return a new {@link MenuDynamic} instance
     */
    public MenuDynamic buildDynamic() {
        MenuDynamic menuDynamic = new MenuDynamic(this.title, new MenuShape(menuShapeType, size), clickable, canExpire, clickActions, this.globalPlaceholders, this.globalMultiLinePlaceholders);
        applyToMenu(menuDynamic);
        return menuDynamic;
    }
}
