package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.Range;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents the drop table of something.
 * <p>
 * Drops can range from items, messages, and commands.
 * </p>
 */
@NoArgsConstructor
public class DropTable implements CASerializable {
    private static final String DEFAULT_GROUP = "@default";
    /**
     * A grouping of drops
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DropGroup implements CASerializable {
        private String _key;
        private List<String> drops;
        private Range dropTargetRange;

        /**
         * Gets a feasible drop target.
         *
         * @return the feasible drop target.
         */
        public int getDropTarget() {
            if (dropTargetRange == null)
                return -1;

            return Math.min(drops.size(), dropTargetRange.getRandomI());
        }
    }

    /**
     * Maps the key of the drop to its instance.
     */
    private Map<String, Drop<?>> table;
    private Map<String, DropGroup> groupings;

    @Override
    public void onDeserialize(ConfigPrimitive primitive) {
        if (groupings == null)
            groupings = new HashMap<>();

        if (!groupings.containsKey(DEFAULT_GROUP)) {
            Set<String> allDrops = new HashSet<>(table.keySet());
            for (var entry : groupings.entrySet()) {
                entry.getValue().getDrops().forEach(allDrops::remove);
            }
            Map<String, Object> unwrapped = primitive.unwrap();
            Range dropTargetRange = null;
            if (unwrapped.containsKey("drop-target-range")) {
                dropTargetRange = ConfigTypeRegistry.getFromType(primitive.getSubValue("drop-target-range"), Range.class);
            }
            groupings.put(DEFAULT_GROUP, new DropGroup(DEFAULT_GROUP, new ArrayList<>(allDrops), dropTargetRange));
        }
    }

    /**
     * Generates a random set of drops.
     *
     * @return the drops.
     */
    public List<Drop<?>> generateDrops() {
        List<Drop<?>> drops = new ArrayList<>();
        for (var groupEntry : groupings.entrySet()) {
            int dropTarget = groupEntry.getValue().getDropTarget();
            int collectedDrops = 0;
            do {
                List<String> dropKeys = new ArrayList<>(groupEntry.getValue().getDrops());
                Collections.shuffle(dropKeys);
                for (String dropKey : dropKeys) {
                    Drop<?> drop = table.get(dropKey);
                    if (!drop.testRandom())
                        continue;

                    drops.add(drop);
                    collectedDrops++;
                    if (dropTarget >= 0 && collectedDrops >= dropTarget)
                        break;
                }
            } while (dropTarget >= 0 && collectedDrops < dropTarget);
        }
        return drops;
    }

    /**
     * Generates drops and gives them to the player.
     *
     * @param player the player.
     */
    public void giveTo(Player player) {
        generateDrops().forEach(drop -> drop.giveTo(player));
    }

    /**
     * Gives the player the drops or drops the drops at the given location.
     *
     * @param player the player.
     */
    public void giveOrDropAt(Player player, Location location) {
        generateDrops().forEach(drop -> {
            if (drop.isPhysical()) {
                drop.dropAt(location);
            } else {
                drop.giveTo(player);
            }
        });
    }
}
