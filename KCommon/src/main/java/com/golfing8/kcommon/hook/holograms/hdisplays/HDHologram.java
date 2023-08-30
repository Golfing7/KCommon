package com.golfing8.kcommon.hook.holograms.hdisplays;

import com.gmail.filoghost.holographicdisplays.api.line.TextLine;
import com.golfing8.kcommon.hook.holograms.Hologram;
import lombok.AllArgsConstructor;

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
    public void setLine(int index, String line) {
        ((TextLine) this.backingHologram.getLine(index)).setText(line);
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
    public int length() {
        return this.backingHologram.size();
    }
}
