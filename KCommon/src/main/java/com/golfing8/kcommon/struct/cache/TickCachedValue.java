package com.golfing8.kcommon.struct.cache;

import com.golfing8.kcommon.NMS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Stores a cached value on a tick time-frame.
 */
@RequiredArgsConstructor
public class TickCachedValue<T> implements CachedValue<T> {
    /** The time, in ticks, it takes for a value to expire. */
    @Getter
    private final long tickExpiryTime;
    /** The tick time-stamp of the last time the value was updated. */
    private long lastTickSet;
    private T value;

    @Override
    public T get() {
        return cacheValid() ? value : null;
    }

    @Override
    public void set(T value) {
        this.lastTickSet = NMS.getTheNMS().getCurrentTick();
        this.value = value;
    }

    @Override
    public boolean cacheValid() {
        return NMS.getTheNMS().getCurrentTick() < lastTickSet + tickExpiryTime;
    }
}
