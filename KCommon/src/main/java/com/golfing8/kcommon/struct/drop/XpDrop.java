package com.golfing8.kcommon.struct.drop;

import com.cryptomorin.xseries.XEnchantment;
import com.golfing8.kcommon.struct.item.FancyItemDrop;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.MathExpressions;
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
    private boolean giveDirectly;
    public XpDrop(double chance,
                  @Nullable String displayName,
                  int xp,
                  boolean giveDirectly) {
        super(chance, displayName);
        this.xp = xp;
        this.giveDirectly = giveDirectly;
    }

    @Override
    public List<Integer> getDrop() {
        return Collections.singletonList(xp);
    }

    @Override
    public void giveTo(Player player) {
        SetExpFix.setTotalExperience(player, SetExpFix.getTotalExperience(player) + xp);
    }

    @Override
    public void dropAt(DropContext context, Location location) {
        if (giveDirectly && context.getPlayer() != null) {
            SetExpFix.setTotalExperience(context.getPlayer(), SetExpFix.getTotalExperience(context.getPlayer()) + xp);
        } else {
            ExperienceOrb orb = location.getWorld().spawn(location, ExperienceOrb.class);
            orb.setExperience(xp);
        }
    }

    @Override
    public boolean isPhysical() {
        return !giveDirectly;
    }
}
