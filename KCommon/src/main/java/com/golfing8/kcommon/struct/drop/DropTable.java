package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.struct.placeholder.MultiLinePlaceholder;
import com.golfing8.kcommon.struct.placeholder.Placeholder;
import com.golfing8.kcommon.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.var;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Represents the drop table of something.
 * <p>
 * Drops can range from items, messages, and commands.
 * </p>
 */
@NoArgsConstructor
@CASerializable.Options(canDelegate = true)
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
        /** The maximum 'tries' that this group does to add a drop. If less than or equal to 0, no maximum is enforced */
        private int maxTries;

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

    public DropTable(Map<String, Drop<?>> drops) {
        table = new HashMap<>(drops);
        groupings = new HashMap<>();
        initDefaultGroup(null, 0);
    }

    public DropTable(Map<String, Drop<?>> drops, Range dropTargetRange) {
        table = new HashMap<>(drops);
        groupings = new HashMap<>();
        initDefaultGroup(dropTargetRange, 0);
    }

    public DropTable(Map<String, Drop<?>> drops, Map<String, DropGroup> groups) {
        table = new HashMap<>(drops);
        groupings = new HashMap<>(groups);
        if (!groupings.containsKey(DEFAULT_GROUP))
            initDefaultGroup(null, 0);
    }

    @Override
    public void onDeserialize(ConfigPrimitive primitive) {
        if (groupings == null)
            groupings = new HashMap<>();

        if (!groupings.containsKey(DEFAULT_GROUP)) {
            Map<String, Object> unwrapped = primitive.unwrap();
            Range dropTargetRange = null;
            if (unwrapped.containsKey("drop-target-range")) {
                dropTargetRange = ConfigTypeRegistry.getFromType(primitive.getSubValue("drop-target-range"), Range.class);
            }
            int maxTries = 0;
            if (unwrapped.containsKey("max-tries")) {
                maxTries = Integer.parseInt(unwrapped.get("max-tries").toString());
            }
            initDefaultGroup(dropTargetRange, maxTries);
        }

    }

    /**
     * Gets all drops from the table
     *
     * @return the drops
     */
    public Collection<Drop<?>> getDrops() {
        return Collections.unmodifiableCollection(table.values());
    }

    /**
     * Initializes the default group in the groupings map.
     *
     * @param dropRange the drop range.
     */
    private void initDefaultGroup(@Nullable Range dropRange, int maxTries) {
        if (groupings.containsKey(DEFAULT_GROUP))
            return;

        Set<String> allDrops = new HashSet<>(table.keySet());
        for (var entry : groupings.entrySet()) {
            entry.getValue().getDrops().forEach(allDrops::remove);
        }
        groupings.put(DEFAULT_GROUP, new DropGroup(DEFAULT_GROUP, new ArrayList<>(allDrops), dropRange, maxTries));
    }

    /**
     * Generates a random set of drops.
     *
     * @return the drops.
     */
    public List<Drop<?>> generateDrops() {
        return generateDrops(DropContext.DEFAULT);
    }

    /**
     * Generates a random set of drops.
     *
     * @param context the drop context
     * @return the drops.
     */
    public List<Drop<?>> generateDrops(@NotNull DropContext context) {
        List<Drop<?>> drops = new ArrayList<>();
        group: for (var groupEntry : groupings.entrySet()) {
            List<String> dropKeys = new ArrayList<>(groupEntry.getValue().getDrops());
            int dropTarget = groupEntry.getValue().getDropTarget();
            int collectedDrops = 0;
            int attempts = 0;
            do {
                Collections.shuffle(dropKeys);
                for (String dropKey : dropKeys) {
                    if (attempts++ > groupEntry.getValue().getMaxTries() && groupEntry.getValue().getMaxTries() > 0)
                        continue group;

                    Drop<?> drop = table.get(dropKey);
                    double totalBoost = Math.min(drop.getMaxBoost(), context.getSpecificBoosts().getOrDefault(dropKey, context.getBoost()));
                    if (!drop.testRandom(totalBoost))
                        continue;

                    drops.add(drop);
                    collectedDrops++;
                    // If the drop target has an upper bound and we've met it, break!
                    if (dropTarget >= 0 && collectedDrops >= dropTarget)
                        break;
                }
            } while (dropTarget >= 0 && collectedDrops < dropTarget); // If we have an upper bound, keep looping til we meet it!
        }
        return drops;
    }

    /**
     * Generates drops and gives them to the player.
     *
     * @param player the player.
     * @return the drops given to the player
     */
    public List<Drop<?>> giveTo(Player player) {
        return giveTo(new DropContext(player), null);
    }

    /**
     * Generates drops and gives them to the player.
     *
     * @param context the context.
     * @return the drops given to the player
     */
    public List<Drop<?>> giveTo(DropContext context) {
        if (context.getPlayer() == null)
            throw new IllegalArgumentException("Player must not be null when giving drops");

        return giveTo(context, null);
    }

    /**
     * Generates drops and gives them to the player.
     *
     * @param player            the player.
     * @param dropMessageFormat the message format for if a drop was given.
     * @return the drops given to the player
     */
    public List<Drop<?>> giveTo(DropContext player, @Nullable Message dropMessageFormat) {
        List<Drop<?>> drops = generateDrops(player);
        drops.forEach(drop -> {
            drop.giveTo(player.getPlayer());
        });
        if (dropMessageFormat != null)
            sendDropMessage(player.getPlayer(), drops, dropMessageFormat);
        return drops;
    }

    /**
     * Gives the player the drops or drops the drops at the given location.
     *
     * @param player   the player.
     * @param location the location
     * @return the drops given to the player
     */
    public List<Drop<?>> giveOrDropAt(Player player, Location location) {
        return giveOrDropAt(new DropContext(player), location, null);
    }

    /**
     * Gives the player the drops or drops the drops at the given location.
     *
     * @param context  the drop context.
     * @param location the location
     * @return the drops given to the player
     */
    public List<Drop<?>> giveOrDropAt(DropContext context, Location location) {
        return giveOrDropAt(context, location, null);
    }

    /**
     * Gives the player the drops or drops the drops at the given location.
     *
     * @param context           the drop context.
     * @param location          the location.
     * @param dropMessageFormat the message format for if a drop was given.
     * @return the drops given to the player
     */
    public List<Drop<?>> giveOrDropAt(DropContext context, Location location, @Nullable Message dropMessageFormat) {
        List<Drop<?>> drops = generateDrops(context);
        drops.forEach(drop -> drop.giveOrDropAt(context, location));
        if (dropMessageFormat != null)
            sendDropMessage(context.getPlayer(), drops, dropMessageFormat);
        return drops;
    }

    /**
     * Sends a message to a player with drop placeholders parsed in.
     * The placeholders parsed in are:
     * <ol>
     * <li>TOTAL_REWARDS - The amount of rewards given total</li>
     * <li>REWARDS - A multi-line placeholder for all the rewards given</li>
     * </ol>
     *
     * @param player  the player.
     * @param drops   the drops parsed in.
     * @param message the drop message.
     */
    public void sendDropMessage(Player player, List<Drop<?>> drops, Message message) {
        List<String> displayNames = new ArrayList<>(); // don't allocate if you don't have to
        drops.forEach(drop -> {
            if (drop.getDisplayName() != null)
                displayNames.add(drop.getDisplayName());
        });
        message.send(player, Placeholder.curlyTrusted("TOTAL_REWARDS", StringUtil.parseCommas(drops.size())), MultiLinePlaceholder.percentTrusted("REWARDS", displayNames));
    }
}
