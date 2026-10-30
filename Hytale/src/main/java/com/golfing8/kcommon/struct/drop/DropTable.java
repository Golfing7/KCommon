package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.Range;
import com.golfing8.kcommon.util.HytalePlayers;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Represents the drop table of something.
 * <p>
 * Drops can range from items, messages, and commands.
 * </p>
 */
@NoArgsConstructor
public class DropTable implements CASerializable {
    private static final String DEFAULT_GROUP = "@default";
    /** Attempt cap when a group sets no MaxTries, so a target that cannot be met never hangs the world thread */
    private static final int UNLIMITED_TRIES_CAP = 10_000;
    private static final DecimalFormat COMMA_FORMAT = new DecimalFormat("###,###,###,###");

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
         * The maximum 'tries' that this group does to add a drop. If less than or equal to 0, no maximum is enforced
         */
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
    private Map<String, Drop<?>> table = new HashMap<>();
    private Map<String, DropGroup> groupings = new HashMap<>();
    private transient Map<String, DropGroup> usedGroupings = new HashMap<>();
    /** Used for the post deserialize method */
    private Range dropTargetRange;
    private int maxTries;

    public DropTable(Map<String, Drop<?>> drops) {
        table = new HashMap<>(drops);
        usedGroupings = new HashMap<>();
        initDefaultGroup(null, 0);
    }

    public DropTable(Map<String, Drop<?>> drops, Range dropTargetRange) {
        table = new HashMap<>(drops);
        usedGroupings = new HashMap<>();
        initDefaultGroup(dropTargetRange, 0);
    }

    public DropTable(Map<String, Drop<?>> drops, Map<String, DropGroup> groups) {
        table = new HashMap<>(drops);
        usedGroupings = new HashMap<>(groups);
        if (!usedGroupings.containsKey(DEFAULT_GROUP))
            initDefaultGroup(null, 0);
    }

    @Override
    public void onDeserialize() {
        usedGroupings = groupings == null ? new HashMap<>() : new HashMap<>(groupings);

        if (!usedGroupings.containsKey(DEFAULT_GROUP)) {
            initDefaultGroup(dropTargetRange, maxTries);
        }
    }

    /**
     * Gets the drop with the given id
     *
     * @param id the id
     * @return the drop
     */
    public @NullableDecl Drop<?> getDrop(String id) {
        return table.get(id);
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
        if (usedGroupings.containsKey(DEFAULT_GROUP))
            return;

        Set<String> allDrops = new HashSet<>(table.keySet());
        for (var entry : usedGroupings.entrySet()) {
            entry.getValue().getDrops().forEach(allDrops::remove);
        }
        usedGroupings.put(DEFAULT_GROUP, new DropGroup(DEFAULT_GROUP, new ArrayList<>(allDrops), dropRange, maxTries));
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
    public List<Drop<?>> generateDrops(@NonNullDecl DropContext context) {
        List<Drop<?>> drops = new ArrayList<>();
        group:
        for (var groupEntry : usedGroupings.entrySet()) {
            List<String> dropKeys = new ArrayList<>(groupEntry.getValue().getDrops());
            int dropTarget = groupEntry.getValue().getDropTarget();
            int collectedDrops = 0;
            int attempts = 0;
            int maxTries = groupEntry.getValue().getMaxTries() > 0 ? groupEntry.getValue().getMaxTries() : UNLIMITED_TRIES_CAP;
            if (dropKeys.isEmpty())
                continue;
            do {
                Collections.shuffle(dropKeys);
                for (String dropKey : dropKeys) {
                    if (attempts++ > maxTries) {
                        if (groupEntry.getValue().getMaxTries() <= 0)
                            Platform.get().getLogger().warning("Drop group '" + groupEntry.getKey() + "' could not reach its drop target; check its drop chances");
                        continue group;
                    }

                    Drop<?> drop = table.get(dropKey);
                    if (drop == null) {
                        Platform.get().getLogger().warning("Drop '" + dropKey + "' not found in drop table under group '" + groupEntry.getKey() + "'");
                        continue;
                    }

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
            drop.giveTo(player);
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

        // If there are no winnings, don't send anything.
        if (displayNames.isEmpty())
            return;

        // TODO Multiline placeholders?
        HytalePlayers.ref(player).sendMessage(message.param("TOTAL_REWARDS", COMMA_FORMAT.format(drops.size())));
    }
}
