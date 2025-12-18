package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * NMS packet access
 */
public interface NMSMagicPackets {
    /**
     * Create a destroy packet with the given ids
     *
     * @param ints the ids
     * @return the packet
     */
    NMSPacket createDestroyPacket(int... ints);

    /**
     * Create a break animation packet
     *
     * @param entityID the id destroying the block
     * @param position the position
     * @param breakStage the break stage
     * @return the packet
     */
    NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage);

    /**
     * Creates a spawn entity packet
     *
     * @param entity the entity
     * @return the packet
     */
    NMSPacket createSpawnEntityPacket(Entity entity);

    /**
     * Creates a spawn living entity packet
     *
     * @param entity the entity
     * @return the packet
     */
    NMSPacket createSpawnEntityLivingPacket(Entity entity);

    /**
     * Creates an update entity metadata packet
     *
     * @param entity the entity
     * @return the packet
     */
    NMSPacket createUpdateEntityMetadataPacket(Entity entity);

    /**
     * Creates an entity effect packet
     *
     * @param entity the entity
     * @param effect the potion effect
     * @return the packet
     */
    NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect);

    /**
     * Creates a remove entity effect packet
     *
     * @param entity the entity
     * @param type the effect type
     * @return the packet
     */
    NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType type);

    /**
     * Sends the packet to the player
     *
     * @param player the player
     * @param packet the packet
     */
    void sendPacket(Player player, NMSPacket packet);
}
