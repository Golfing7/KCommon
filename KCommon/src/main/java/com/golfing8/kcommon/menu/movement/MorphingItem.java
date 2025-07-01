package com.golfing8.kcommon.menu.movement;

import com.golfing8.kcommon.menu.Menu;
import com.golfing8.kcommon.struct.ResettableNumber;
import com.golfing8.kcommon.struct.SoundWrapper;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

public class MorphingItem {

    private final Menu menu;
    private final ItemStack[] progression;
    private final int progressionLength;
    private final int slot;
    @Getter
    private final ResettableNumber speed;
    @Getter
    private final ResettableNumber rolloverSpeed;
    private final SoundWrapper soundWrapper;
    private int currentSpot;
    private boolean removeOnFinish;
    @Getter @Setter
    private boolean frozen;

    //Internal field used for rollover
    private boolean justReset;

    public MorphingItem(Menu menu, ItemStack[] progression, int slot, int speed, int rolloverSpeed, boolean removeOnFinish, SoundWrapper soundWrapper) {
        if (removeOnFinish) {
            rolloverSpeed = -1;
        }
        this.menu = menu;
        this.progressionLength = progression.length;
        this.slot = slot;
        this.progression = progression;
        this.speed = new ResettableNumber(speed);
        this.rolloverSpeed = new ResettableNumber(rolloverSpeed);
        this.soundWrapper = soundWrapper;
        this.removeOnFinish = removeOnFinish;
    }

    public void reset() {
        this.currentSpot = 0;
    }

    public boolean morphCheck() {
        if (currentSpot + 1 >= progressionLength) {
            if (rolloverSpeed.getCurrentValue() == -1) {
                if (removeOnFinish) {
                    menu.setItemAt(slot, null);
                    removeOnFinish = false;
                }
                return false;
            }
            if (rolloverSpeed.getCurrentValue() > 0) {
                rolloverSpeed.setCurrentValue(rolloverSpeed.getCurrentValue() - 1);
                return false;
            } else {
                rolloverSpeed.reset();
                currentSpot = 0;
                justReset = true;
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

    public void progress() {
        if (justReset) {
            justReset = false;
            return;
        }

        ++currentSpot;
    }

    public ItemStack getCurrent() {
        return progression[currentSpot];
    }

    public void morph() {
        if (frozen)
            return;

        if (!justReset && ++currentSpot >= progressionLength) {
            return;
        }

        justReset = false;

        menu.setItemAt(slot, progression[currentSpot]);

        if (soundWrapper != null) {
            menu.getViewers().forEach(z -> z.playSound(z.getLocation(), this.soundWrapper.getSound().parseSound(), this.soundWrapper.getVolume(), this.soundWrapper.getPitch()));
        }
    }

    public static class Builder {
        private int rolloverSpeed = 0, speed = 0, slot;
        private ItemStack[] morphSet;
        private SoundWrapper sound;
        private boolean removeOnFinish;

        private Menu menu;

        private Builder() {
        }

        public static Builder builder(Menu menu, ItemStack[] morphSet, int slot) {
            Builder builder = new Builder();
            builder.slot = slot;
            builder.menu = menu;
            builder.morphSet = morphSet;
            return builder;
        }

        public Builder rolloverSpeed(int rolloverSpeed) {
            this.rolloverSpeed = rolloverSpeed;
            return this;
        }

        public Builder removeOnFinish(boolean removeOnFinish) {
            this.removeOnFinish = removeOnFinish;
            return this;
        }

        public Builder withSound(SoundWrapper wrapper) {
            this.sound = wrapper;
            return this;
        }

        public Builder speed(int speed) {
            this.speed = speed;
            return this;
        }

        public MorphingItem build() {
            return new MorphingItem(menu, morphSet, slot, speed, rolloverSpeed, removeOnFinish, sound);
        }
    }
}
