package com.golfing8.kcommon.util;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

/**
 * A utility class for interacting with EXP in a 'fixed' way.
 */
@UtilityClass
public class SetExpFix {
    private static final Int2IntMap EXP_CACHE = new Int2IntOpenHashMap();

    static {
        // Cache for the first 1000 levels of XP cost
        for (int i = 1; i <= 1000; i++) {
            getExpToLevel(i);
        }
    }

    /**
     * Sets the total experience of the given player
     *
     * @param player the player
     * @param exp the xp
     */
    public static void setTotalExperience(final Player player, final int exp) {
        if (exp < 0) {
            throw new IllegalArgumentException("Experience is negative!");
        }
        player.setExp(0);
        player.setLevel(0);
        player.setTotalExperience(0);

        //This following code is technically redundant now, as bukkit now calulcates levels more or less correctly
        //At larger numbers however... player.getExp(3000), only seems to give 2999, putting the below calculations off.
        int amount = exp;
        while (amount > 0) {
            final int expToLevel = getExpAtLevel(player);
            amount -= expToLevel;
            if (amount >= 0) {
                // give until next level
                player.giveExp(expToLevel);
            } else {
                // give the rest
                amount += expToLevel;
                player.giveExp(amount);
                amount = 0;
            }
        }
    }

    private static int getExpAtLevel(final Player player) {
        return getExpAtLevel(player.getLevel());
    }

    /**
     * Gets the EXP required for the given level
     *
     * @param level the level
     * @return the exp
     */
    //new Exp Math from 1.8
    public static int getExpAtLevel(final int level) {
        if (level <= 15) {
            return (2 * level) + 7;
        }
        if (level <= 30) {
            return (5 * level) - 38;
        }
        return (9 * level) - 158;

    }

    /**
     * Gets the EXP required to get to the given level
     *
     * @param level the level
     * @return the EXP
     */
    public static int getExpToLevel(final int level) {
        if (EXP_CACHE.containsKey(level)) {
            return EXP_CACHE.get(level);
        }
        int currentLevel = 0;
        int exp = 0;

        while (currentLevel < level) {
            exp += getExpAtLevel(currentLevel);
            currentLevel++;
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        EXP_CACHE.put(level, exp);
        return exp;
    }

    /**
     * Gets the total experience for the given player
     *
     * @param player the player
     * @return the experience
     */
    // This method is required because the bukkit player.getTotalExperience() method, shows exp that has been 'spent'.
    // Without this people would be able to use exp and then still sell it.
    public static int getTotalExperience(final Player player) {
        int exp = Math.round(getExpAtLevel(player) * player.getExp());
        int currentLevel = player.getLevel();

        while (currentLevel > 0) {
            currentLevel--;
            exp += getExpAtLevel(currentLevel);
        }
        if (exp < 0) {
            exp = Integer.MAX_VALUE;
        }
        return exp;
    }

    /**
     * Gets the exp required for the player to reach the next level
     *
     * @param player the player
     * @return the exp for the next level
     */
    public static int getExpUntilNextLevel(final Player player) {
        final int exp = Math.round(getExpAtLevel(player) * player.getExp());
        final int nextLevel = player.getLevel();
        return getExpAtLevel(nextLevel) - exp;
    }
}
