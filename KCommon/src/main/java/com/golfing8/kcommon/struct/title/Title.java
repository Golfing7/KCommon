package com.golfing8.kcommon.struct.title;

import com.golfing8.kcommon.NMS;
import lombok.Data;
import org.bukkit.entity.Player;

/**
 * A wrapper class for a title that can be sent to a player.
 */
@Data
public class Title {
    public static final int DEFAULT_IN = 10, DEFAULT_STAY = 60, DEFAULT_OUT = 10;

    /**
     * The title to show to the player.
     */
    private final String title;
    /**
     * The subtitle to show to the player.
     */
    private final String subtitle;
    /**
     * The amount of time, in ticks, for the title to appear.
     */
    private final int in;
    /**
     * The amount of time, in ticks, the title will stay.
     */
    private final int stay;
    /**
     * The amount of time, in ticks, the title will take to remove.
     */
    private final int out;

    /**
     * Sends the given title to the player.
     *
     * @param player the player.
     */
    public void send(Player player) {
        NMS.getTheNMS().sendTitle(player, title, subtitle, in, stay, out);
    }
}
