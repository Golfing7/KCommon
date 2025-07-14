package com.golfing8.kcommon.data;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * An abstract data serializable class that is backed by a player.
 */
public abstract class SenderSerializable extends AbstractSerializable {
    /**
     * The player's UUID.
     */
    private transient UUID playerUUID;

    @Override
    public String getKey() {
        if (playerUUID == null)
            playerUUID = UUID.fromString(super.getKey());
        return playerUUID.toString();
    }

    @Override
    public void setKey(String objectId) {
        super.setKey(objectId);
        playerUUID = UUID.fromString(objectId);
    }

    public UUID getPlayerUUID() {
        if (playerUUID == null)
            playerUUID = UUID.fromString(super.getKey());
        return playerUUID;
    }

    /**
     * Gets an online version of this player, or null if they're not online.
     *
     * @return the player.
     */
    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(playerUUID);
    }
}
