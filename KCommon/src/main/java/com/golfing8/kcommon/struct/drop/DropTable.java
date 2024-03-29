package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.config.adapter.CASerializable;
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
public class DropTable implements CASerializable {
    /**
     * Maps the key of the drop to its instance.
     */
    private Map<String, Drop<?>> dropTable;
    /**
     * Maps drops from their group name.
     */
    private transient Map<String, List<Drop<?>>> dropGroups = new HashMap<>();

    /**
     * Generates a random set of drops.
     *
     * @return the drops.
     */
    public List<Drop<?>> generateDrops() {
        List<Drop<?>> drops = new ArrayList<>();
        for (var entry : dropGroups.entrySet()) {
            List<Drop<?>> dropList = new ArrayList<>(entry.getValue());
            while (!dropList.isEmpty()) {
                int index = ThreadLocalRandom.current().nextInt(dropList.size());
                Drop<?> drop = dropList.get(index);
                if (!drop.testRandom())
                    continue;

                drops.add(drop);
                break;
            }
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
