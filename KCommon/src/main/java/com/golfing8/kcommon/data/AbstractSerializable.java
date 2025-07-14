package com.golfing8.kcommon.data;

import lombok.Getter;

public abstract class AbstractSerializable implements DataSerializable {
    /**
     * The ID of this  object
     */
    @Getter
    private String _objectId;
    /**
     * If this object has changed
     */
    @Getter
    private transient boolean changed;

    @Override
    public String getKey() {
        return _objectId;
    }

    @Override
    public void setKey(String key) {
        this._objectId = key;
    }

    @Override
    public void change() {
        this.changed = true;
    }

    @Override
    public void markSaved() {
        this.changed = false;
    }

    @Override
    public boolean hasChanged() {
        return changed;
    }
}
