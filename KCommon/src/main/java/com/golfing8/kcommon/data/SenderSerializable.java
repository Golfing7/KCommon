package com.golfing8.kcommon.data;

import com.google.gson.JsonObject;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An abstract data serializable class that is backed by a player.
 */
public abstract class SenderSerializable extends AbstractSerializable {
    /** The player's UUID. */
    @Getter
    private UUID playerUUID;
    /** The cached player object */
    private transient Player player;

    @Override
    public String getKey() {
        return playerUUID.toString();
    }

    @Override
    public void setKey(String objectId) {
        super.setKey(objectId);
        playerUUID = UUID.fromString(objectId);
    }

    /**
     * Gets an online version of this player, or null if they're not online.
     *
     * @return the player.
     */
    @Nullable
    public Player getPlayer() {
        if (player != null) {
            if (!player.isOnline())
                return null;
            return player;
        }
        return player = Bukkit.getPlayer(playerUUID);
    }
}
