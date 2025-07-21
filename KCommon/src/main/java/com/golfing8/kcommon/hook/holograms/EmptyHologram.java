package com.golfing8.kcommon.hook.holograms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * An empty implementation of {@link Hologram}
 */
public class EmptyHologram implements Hologram {
    @Override
    public boolean isVisibleByDefault() {
        return false;
    }

    @Override
    public void setVisibleByDefault(boolean visible) {

    }

    @Override
    public void setVisibleTo(Player player) {

    }

    @Override
    public void removeVisibleTo(Player player) {

    }

    @Override
    public void setHideTo(Player player) {

    }

    @Override
    public void removeHideTo(Player player) {

    }

    @Override
    public void setLine(int index, String line) {

    }

    @Override
    public void setLine(int index, ItemStack line) {

    }

    @Override
    public void removeLine(int index) {

    }

    @Override
    public void clearLines() {

    }

    @Override
    public void delete() {

    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public void addLine(int index, String line) {

    }

    @Override
    public void addLine(int index, ItemStack itemStack) {

    }

    @Override
    public void setLines(List<String> lines) {

    }

    @Override
    public int length() {
        return 0;
    }
}
