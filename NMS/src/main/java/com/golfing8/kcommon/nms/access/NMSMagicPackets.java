package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public interface NMSMagicPackets {
    NMSPacket createDestroyPacket(int... ints);

    NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage);

    NMSPacket createSpawnEntityPacket(Entity entity);

    NMSPacket createSpawnEntityLivingPacket(Entity entity);

    NMSPacket createUpdateEntityMetadataPacket(Entity entity);

    NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect);

    NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType type);

    void sendPacket(Player player, NMSPacket packet);
}
