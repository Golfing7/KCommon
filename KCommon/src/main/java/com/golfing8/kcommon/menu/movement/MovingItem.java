package com.golfing8.kcommon.menu.movement;

import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.menu.MenuUtils;
import com.golfing8.kcommon.menu.action.ClickAction;
import com.golfing8.kcommon.menu.action.ClickRunnable;
import com.golfing8.kcommon.menu.shape.MenuCoordinate;
import com.golfing8.kcommon.struct.ResettableNumber;
import com.golfing8.kcommon.struct.SoundWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * An item that is moving within a {@link com.golfing8.kcommon.menu.MenuDynamic}
 */
public class MovingItem {

    private final int finalStep;
    @Getter
    private final ResettableNumber speed;
    private final MorphingItem morphingItem;
    private final Menu menu;
    private final ResettableNumber rolloverSpeed;
    private final boolean nonDestructive;
    private final boolean copySelf;
    private final SoundWrapper soundWrapper;
    private ItemStack originalItem;
    private MenuCoordinate[] slotProgression;
    private int currentStep;
    private ItemStack previousStack;
    private boolean removeOnFinish;
    @Getter
    @Setter
    private boolean frozen;

    @Getter
    private final List<ClickAction> clickActions;

    private boolean justReset, justOpened;

    public MovingItem(Menu menu, int speed, MoveLength[] moveLengths, ItemStack[] stacks, List<ClickAction> clickActions, boolean nonDestructive, boolean copySelf, boolean removeOnFinish, int rolloverSpeed, SoundWrapper soundWrapper) {
        compileLengths(moveLengths);
        if (removeOnFinish) {
            rolloverSpeed = -1;
        }
        this.clickActions = clickActions;
        this.speed = new ResettableNumber(speed);
        this.rolloverSpeed = new ResettableNumber(rolloverSpeed);
        this.finalStep = slotProgression.length;
        this.currentStep = 0;
        this.morphingItem = new MorphingItem(menu, stacks, MenuUtils.getSlotFromCartCoords(menu.getMenuShape().getType(), moveLengths[0].getCoordinates().get(0).getX(), moveLengths[0].getCoordinates().get(0).getY()), 0, 0, false, null);
        this.menu = menu;
        this.nonDestructive = nonDestructive;
        this.copySelf = copySelf;
        this.soundWrapper = soundWrapper;

        this.justOpened = true;
    }

    private void compileLengths(MoveLength... moveLengths) {
        int totalLength = 0;
        for (MoveLength array : moveLengths) {
            totalLength += array.getCoordinates().size();
        }

        MenuCoordinate[] coordinates = new MenuCoordinate[totalLength];

        int index = 0;

        for (MoveLength array : moveLengths) {
            System.arraycopy(array.getCoordinates().toArray(new MenuCoordinate[0]), 0, coordinates, index, array.getCoordinates().size());

            index += array.getCoordinates().size();
        }

        slotProgression = coordinates;
    }

    public int getCurrentSlot() {
        return MenuUtils.getSlotFromCartCoords(menu.getMenuShape().getType(), slotProgression[currentStep].getX(), slotProgression[currentStep].getY());
    }

    /**
     * Resets the item back to its original location
     */
    public void reset() {
        this.currentStep = 0;
        this.morphingItem.reset();
        this.menu.setItemAt(slotProgression[0].getX(), slotProgression[0].getY(), morphingItem.getCurrent());
    }

    /**
     * Checks if this item can move
     *
     * @return true if it can move
     */
    public boolean canMove() {
        if (currentStep + 1 >= finalStep) {
            if (speed.getCurrentValue() > 0) {
                speed.setCurrentValue(speed.getCurrentValue() - 1);
                return false;
            }
            if (rolloverSpeed.getCurrentValue() == -1) {
                if (removeOnFinish) {
                    menu.setItemAt(MenuUtils.getSlotFromCartCoords(menu.getMenuShape().getType(), slotProgression[currentStep].getX(), slotProgression[currentStep].getY()), null);

                    removeOnFinish = false;
                }
                return false;
            }
            if (rolloverSpeed.getCurrentValue() > 0) {
                if (rolloverSpeed.getCurrentValue() == rolloverSpeed.getHeldValue() && nonDestructive) {
                    menu.setItemAt(slotProgression[finalStep - 1].getX(), slotProgression[finalStep - 1].getY(), previousStack);
                }
                rolloverSpeed.setCurrentValue(rolloverSpeed.getCurrentValue() - 1);
                return false;
            } else {
                rolloverSpeed.reset();
                this.reset();
                justReset = true;
                speed.reset();
                return true;
            }
        } else {
            if (speed.getCurrentValue() <= 0) {
                speed.reset();
                return true;
            } else {
                speed.setCurrentValue(speed.getCurrentValue() - 1);
                return false;
            }
        }
    }

    /**
     * Moves the item to the next location
     */
    public void move() {
        if (frozen)
            return;

        if (!justReset && !justOpened && ++currentStep >= finalStep) {
            return;
        }

        MenuCoordinate nextCoordinates = slotProgression[currentStep];
        if (justOpened) {
            originalItem = menu.getItemAt(nextCoordinates.getX(), nextCoordinates.getY());
        }

        justOpened = false;
        if (!copySelf) {
            if (currentStep > 0) {
                MenuCoordinate previousCoordinates = slotProgression[currentStep - 1];

                menu.setItemAt(previousCoordinates.getX(), previousCoordinates.getY(), nonDestructive ? currentStep == 1 ? originalItem : previousStack : null);
            } else if (justReset) {
                MenuCoordinate last = slotProgression[finalStep - 1];

                menu.setItemAt(last.getX(), last.getY(), nonDestructive ? previousStack : null);
            }
        }

        justReset = false;
        previousStack = menu.setItemAt(nextCoordinates.getX(), nextCoordinates.getY(), morphingItem.getCurrent());
        morphingItem.morphCheck();
        morphingItem.progress();
        if (soundWrapper != null) {
            menu.getViewers().forEach(z -> z.playSound(z.getLocation(), this.soundWrapper.getSound().get(), this.soundWrapper.getVolume(), this.soundWrapper.getPitch()));
        }
    }

    /**
     * A builder for moving item instances
     */
    public static final class Builder {
        private ItemStack[] stacks;
        private boolean nonDestructive, copySelf, removeOnFinish;
        private int rolloverSpeed = 0, speed = 0;
        private MoveLength[] moveLengths;
        private SoundWrapper soundWrapper;
        private List<ClickAction> clickActions;
        private Menu menu;

        private Builder() {
        }

        /**
         * Creates a builder with the basic information
         *
         * @param menu the menu
         * @param moveLengths the movements
         * @param stacks the items to be applied
         * @return the builder
         */
        public static Builder builder(Menu menu, MoveLength[] moveLengths, ItemStack[] stacks) {
            Builder builder = new Builder();
            builder.stacks = stacks;
            builder.clickActions = new ArrayList<>();
            builder.menu = menu;
            builder.moveLengths = moveLengths;
            return builder;
        }

        /**
         * If this item should be removed once it finished moving
         *
         * @param removeOnFinish if the item should be removed
         * @return this
         */
        public Builder removeOnFinish(boolean removeOnFinish) {
            this.removeOnFinish = removeOnFinish;
            return this;
        }

        /**
         * If the item should copy itself to the new location, rather than just moving
         *
         * @param copySelf copy instead of move
         * @return this
         */
        public Builder copySelf(boolean copySelf) {
            this.copySelf = copySelf;
            return this;
        }

        /**
         * Adds the click event task to this item.
         * It will follow it wherever it moves
         *
         * @param runnable the task
         * @return this
         */
        public Builder addClickAction(ClickRunnable runnable) {
            clickActions.add(new ClickAction(200L, runnable));
            return this;
        }

        /**
         * Adds the given click action to this item
         *
         * @param action the action
         * @return this
         */
        public Builder addClickAction(ClickAction action) {
            clickActions.add(action);
            return this;
        }

        /**
         * If non-destructive, the item will place the old items back where they were when
         * it moves off of a slot
         *
         * @param nonDestructive if non-destructive movements
         * @return this
         */
        public Builder nonDestructive(boolean nonDestructive) {
            this.nonDestructive = nonDestructive;
            return this;
        }

        /**
         * The amount of time before repeating the entire animation cycle
         *
         * @param rolloverSpeed the rollover speed
         * @return this
         */
        public Builder rolloverSpeed(int rolloverSpeed) {
            this.rolloverSpeed = rolloverSpeed;
            return this;
        }

        /**
         * The period in ticks movements will take place
         *
         * @param speed the speed
         * @return this
         */
        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        /**
         * When the item moves, this sound plays
         *
         * @param sound the sound
         * @return this
         */
        public Builder withSound(SoundWrapper sound) {
            this.soundWrapper = sound;
            return this;
        }

        /**
         * Builds a moving item from the settings
         *
         * @return the built item
         */
        public MovingItem build() {
            return new MovingItem(menu, speed, moveLengths, stacks, clickActions, nonDestructive, copySelf, removeOnFinish, rolloverSpeed, soundWrapper);
        }
    }
}
