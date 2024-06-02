package com.golfing8.kcommon.hook.holograms.dholograms;

import com.golfing8.kcommon.hook.holograms.Hologram;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.DisableCause;
import eu.decentsoftware.holograms.api.holograms.HologramPage;
import lombok.AllArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Implements a hologram using Decent Holograms
 */
@AllArgsConstructor
public class DHHologram implements Hologram {
    private final eu.decentsoftware.holograms.api.holograms.Hologram backingHologram;

    @Override
    public void setLine(int index, String line) {
        DHAPI.setHologramLine(this.backingHologram, index, line);
    }

    @Override
    public void setLine(int index, ItemStack line) {
        DHAPI.setHologramLine(this.backingHologram, index, line);
    }

    @Override
    public void removeLine(int index) {
        DHAPI.removeHologramLine(this.backingHologram, index);
    }

    @Override
    public void clearLines() {
        HologramPage page = this.backingHologram.getPage(0);
        while(page.size() > 0)
            page.removeLine(0);
    }

    @Override
    public void delete() {
        this.backingHologram.delete();
    }

    @Override
    public boolean isDeleted() {
        return this.backingHologram.getDisableCause() != DisableCause.NONE;
    }

    @Override
    public void addLine(int index, String line) {
        if(index == this.length())
            DHAPI.addHologramLine(backingHologram, line);
        else
            DHAPI.insertHologramLine(backingHologram, index, line);
    }

    @Override
    public void addLine(int index, ItemStack itemStack) {
        if(index == this.length())
            DHAPI.addHologramLine(backingHologram, itemStack);
        else
            DHAPI.insertHologramLine(backingHologram, index, itemStack);
    }

    @Override
    public void setLines(List<String> lines) {
        DHAPI.setHologramLines(backingHologram, lines);
    }

    @Override
    public int length() {
        return this.backingHologram.getPage(0).size();
    }
}
