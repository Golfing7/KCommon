package com.golfing8.kcommon.hook.holograms.hdisplays;

import com.gmail.filoghost.holographicdisplays.api.line.ItemLine;
import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.golfing8.kcommon.hook.holograms.Hologram;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Implements a hologram using Holographic Displays.
 */
@AllArgsConstructor
public class HDHologram implements Hologram {
    /**
     * The backing hologram
     */
    private final com.gmail.filoghost.holographicdisplays.api.Hologram backingHologram;

    @Override
    public boolean isVisibleByDefault() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVisibleByDefault(boolean visible) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setVisibleTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeVisibleTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setHideTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeHideTo(Player player) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLine(int index, String line) {
        if (this.backingHologram.getLine(index) instanceof TextLine) {
            ((TextLine) this.backingHologram.getLine(index)).setText(line);
        } else {
            this.backingHologram.removeLine(index);
            this.backingHologram.insertTextLine(index, line);
        }
    }

    @Override
    public void setLine(int index, ItemStack line) {
        if (this.backingHologram.getLine(index) instanceof ItemLine) {
            ((ItemLine) this.backingHologram.getLine(index)).setItemStack(line);
        } else {
            this.backingHologram.removeLine(index);
            this.backingHologram.insertItemLine(index, line);
        }
    }

    @Override
    public void removeLine(int index) {
        this.backingHologram.removeLine(index);
    }

    @Override
    public void clearLines() {
        this.backingHologram.clearLines();
    }

    @Override
    public void delete() {
        this.backingHologram.delete();
    }

    @Override
    public boolean isDeleted() {
        return this.backingHologram.isDeleted();
    }

    @Override
    public void addLine(int index, String line) {
        this.backingHologram.insertTextLine(index, line);
    }

    @Override
    public void addLine(int index, ItemStack itemStack) {
        this.backingHologram.insertItemLine(index, itemStack);
    }

    @Override
    public void setLines(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i >= this.length()) {
                addLine(line);
            } else {
                setLine(i, line);
            }
        }

        // Make sure we remove extra lines.
        for (int i = lines.size(); i < length(); i++) {
            removeLine(i);
        }
    }

    @Override
    public int length() {
        return this.backingHologram.size();
    }
}
