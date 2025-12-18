package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.access.NMSMagicPackets;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.v1_8.packets.*;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * NMS 1.8 packet access
 */
public class MagicPacketsV1_8 implements NMSMagicPackets {
    @Override
    public NMSPacket createDestroyPacket(int... ints) {
        return new OutEntityDestroyV1_8(new PacketPlayOutEntityDestroy(ints));
    }

    @Override
    public NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage) {
        return new OutBreakAnimationV1_8(new PacketPlayOutBlockBreakAnimation(
                entityID,
                new BlockPosition(position.getX(), position.getY(), position.getZ()),
                breakStage
        ));
    }

    @Override
    public NMSPacket createSpawnEntityPacket(Entity entity) {
        PacketPlayOutSpawnEntity spawnEntity = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 0);
        return new OutSpawnEntityV1_8(spawnEntity);
    }

    @Override
    public NMSPacket createSpawnEntityLivingPacket(Entity entity) {
        PacketPlayOutSpawnEntityLiving spawnEntity = new PacketPlayOutSpawnEntityLiving(((CraftLivingEntity) entity).getHandle());
        return new OutSpawnEntityLivingV1_8(spawnEntity);
    }

    @Override
    public NMSPacket createUpdateEntityMetadataPacket(Entity entity) {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getEntityId(),
                ((CraftEntity) entity).getHandle().getDataWatcher(),
                false);
        return new OutEntityMetadataV1_8(metadata);
    }

    @Override
    public NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect) {
        MobEffect me = new MobEffect(effect.getType().getId(), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());

        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(entity.getEntityId(), me);
        return new OutEntityEffectV1_8(entityEffect);
    }

    @Override
    public NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType effect) {
        MobEffect me = new MobEffect(effect.getId(), 0);

        PacketPlayOutRemoveEntityEffect entityEffect = new PacketPlayOutRemoveEntityEffect(entity.getEntityId(), me);
        return new OutRemoveEntityEffectV1_8(entityEffect);
    }

    @Override
    public void sendPacket(Player player, NMSPacket packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet) packet.getHandle());
    }
}
