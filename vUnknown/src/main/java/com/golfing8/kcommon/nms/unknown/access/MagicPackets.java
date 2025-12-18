package com.golfing8.kcommon.nms.unknown.access;

import com.golfing8.kcommon.nms.access.NMSMagicPackets;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * API agnostic packet access
 * No methods are supported for this version
 */
public class MagicPackets implements NMSMagicPackets {
    @Override
    public NMSPacket createDestroyPacket(int... ints) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createSpawnEntityPacket(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createSpawnEntityLivingPacket(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createUpdateEntityMetadataPacket(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType effect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void sendPacket(Player player, NMSPacket packet) {
        throw new UnsupportedOperationException();
    }
}
