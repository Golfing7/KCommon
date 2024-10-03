package com.golfing8.kcommon.struct.drop;

import com.cryptomorin.xseries.XEnchantment;
import com.golfing8.kcommon.struct.item.FancyItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.MathExpressions;
import com.golfing8.kcommon.util.MathUtil;
import com.golfing8.kcommon.util.PlayerUtil;
import com.golfing8.kcommon.util.SetExpFix;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a Drop for XP.
 */
@Getter
public class XpDrop extends Drop<Integer> {
    private int xp;
    private boolean boostQuantity;
    private boolean giveDirectly;
    public XpDrop(double chance,
                  @Nullable String displayName,
                  int xp,
                  boolean boostQuantity,
                  boolean giveDirectly) {
        super(chance, displayName);
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
        int xpToGive = boostQuantity ? MathUtil.roundRandomly(xp * context.getBoost()) : xp;
        SetExpFix.setTotalExperience(context.getPlayer(), SetExpFix.getTotalExperience(context.getPlayer()) + xpToGive);
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        int xpToGive = boostQuantity ? MathUtil.roundRandomly(xp * context.getBoost()) : xp;
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
