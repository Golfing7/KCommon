package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicBlocks;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_8_R3.block.CraftSkull;

public class MagicBlocksV1_8 implements NMSMagicBlocks {
    @SuppressWarnings("unchecked")
    private final FieldHandle<GameProfile> skullProfileField = (FieldHandle<GameProfile>) FieldHandles.getHandle("profile", CraftSkull.class);

    @Override
    public void setSkullOwner(Skull skull, OfflinePlayer offlinePlayer) {
        skullProfileField.set(skull, new GameProfile(offlinePlayer.getUniqueId(), offlinePlayer.getName()));
    }
}
