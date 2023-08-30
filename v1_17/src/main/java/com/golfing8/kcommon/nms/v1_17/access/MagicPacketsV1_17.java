package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.access.NMSMagicPackets;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.v1_17.packets.*;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MagicPacketsV1_17 implements NMSMagicPackets {
    @Override
    public NMSPacket createDestroyPacket(int... ints) {
        return new OutEntityDestroyV1_17(new PacketPlayOutEntityDestroy(ints));
    }

    @Override
    public NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage) {
        return new OutBreakAnimationV1_17(new PacketPlayOutBlockBreakAnimation(
                entityID,
                new BlockPosition(position.getX(), position.getY(), position.getZ()),
                breakStage
        ));
    }

    @Override
    public NMSPacket createSpawnEntityPacket(Entity entity) {
        PacketPlayOutSpawnEntity spawnEntity = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 0);
        return new OutSpawnEntityV1_17(spawnEntity);
    }

    @Override
    public NMSPacket createUpdateEntityMetadataPacket(Entity entity) {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getEntityId(),
                ((CraftEntity) entity).getHandle().getDataWatcher(),
                false);
        return new OutEntityMetadataV1_17(metadata);
    }

    @Override
    public NMSPacket createSpawnEntityLivingPacket(Entity entity) {
        PacketPlayOutSpawnEntityLiving spawnEntity = new PacketPlayOutSpawnEntityLiving(((CraftLivingEntity) entity).getHandle());
        return new OutSpawnEntityLivingV1_17(spawnEntity);
    }

    @Override
    public NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect) {
        MobEffect me = new MobEffect(MobEffectList.fromId(effect.getType().getId()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());

        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(entity.getEntityId(), me);
        return new OutEntityEffectV1_17(entityEffect);
    }

    @Override
    public NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType effect) {
        PacketPlayOutRemoveEntityEffect entityEffect = new PacketPlayOutRemoveEntityEffect(entity.getEntityId(), MobEffectList.fromId(effect.getId()));
        return new OutRemoveEntityEffectV1_17(entityEffect);
    }

    @Override
    public void sendPacket(Player player, NMSPacket packet) {
        ((CraftPlayer) player).getHandle().b.sendPacket((Packet<?>) packet.getHandle());
    }
}
