package com.golfing8.kcommon.hook.holograms;

/**
 * An empty implementation of {@link Hologram}
 */
public class EmptyHologram implements Hologram {
    @Override
    public void setLine(int index, String line) {

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
    public int length() {
        return 0;
    }
}
