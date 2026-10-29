package com.golfing8.kcommon.struct.region.ruled;

import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.struct.helper.terminable.composite.CompositeTerminable;
import lombok.Getter;

/**
 * A rule enforced inside a {@link RuledRegion}.
 */
public abstract class RegionRule {
    @Getter
    private RuledRegion region;
    private CompositeTerminable enforcement;

    /**
     * Starts enforcing this rule in the region.
     *
     * @param region the region.
     */
    public final void register(RuledRegion region) {
        if (this.region != null)
            throw new IllegalStateException("Cannot register rule twice!");

        this.region = region;
        this.enforcement = CompositeTerminable.create();
        startEnforcing(enforcement);
    }

    /**
     * Stops enforcing this rule.
     */
    public final void shutdown() {
        if (this.region == null)
            throw new IllegalStateException("Region not registered.");

        this.enforcement.closeAndReportException();
        stopEnforcing();
        this.region = null;
    }

    /**
     * Starts listening for whatever this rule enforces. Bind every listener and task to the given terminable.
     *
     * @param enforcement closed when the rule stops.
     */
    protected abstract void startEnforcing(CompositeTerminable enforcement);

    /**
     * Called after the rule stopped enforcing.
     */
    protected void stopEnforcing() {
    }

    /**
     * Binds the terminable to this rule's enforcement.
     *
     * @param terminable the terminable.
     */
    protected final void bind(Terminable terminable) {
        enforcement.bind(terminable);
    }
}
