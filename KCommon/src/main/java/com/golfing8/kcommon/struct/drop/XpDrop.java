package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.util.MathUtil;
import com.golfing8.kcommon.util.SetExpFix;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents a Drop for XP.
 */
@Getter
public class XpDrop extends Drop<Integer> {
    private final int xp;
    private final boolean boostQuantity;
    private final boolean giveDirectly;

    public XpDrop(double chance,
                  @Nullable String displayName,
                  double maxBoost,
                  int xp,
                  boolean boostQuantity,
                  boolean giveDirectly) {
        super(chance, displayName, maxBoost);
        this.xp = xp;
        this.boostQuantity = boostQuantity;
        this.giveDirectly = giveDirectly;
    }

    @Override
    public List<Integer> getDrop() {
        return Collections.singletonList(xp);
    }

    @Override
    public void giveTo(DropContext context) {
        double effectiveBoost = Math.min(context.getBoost(), getMaxBoost());
        int xpToGive = boostQuantity ? MathUtil.roundRandomly(xp * effectiveBoost) : xp;
        SetExpFix.setTotalExperience(context.getPlayer(), SetExpFix.getTotalExperience(context.getPlayer()) + xpToGive);
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        double effectiveBoost = Math.min(context.getBoost(), getMaxBoost());
        int xpToGive = boostQuantity ? MathUtil.roundRandomly(xp * effectiveBoost) : xp;
        if (giveDirectly && context.getPlayer() != null) {
            SetExpFix.setTotalExperience(context.getPlayer(), SetExpFix.getTotalExperience(context.getPlayer()) + xpToGive);
        } else {
            ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
            orb.setExperience(xpToGive);
        }
    }

    @Override
    public boolean isPhysical() {
        return !giveDirectly;
    }
}
