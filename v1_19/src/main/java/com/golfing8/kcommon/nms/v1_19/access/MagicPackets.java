package com.golfing8.kcommon.nms.v1_19.access;

import com.golfing8.kcommon.nms.access.NMSMagicPackets;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.struct.Position;
import com.golfing8.kcommon.nms.v1_19.packets.*;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectList;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MagicPackets implements NMSMagicPackets {
    @Override
    public NMSPacket createDestroyPacket(int... ints) {
        return new OutEntityDestroy(new PacketPlayOutEntityDestroy(ints));
    }

    @Override
    public NMSPacket createBreakAnimationPacket(int entityID, Position position, int breakStage) {
        return new OutBreakAnimation(new PacketPlayOutBlockBreakAnimation(
                entityID,
                new BlockPosition(position.getX(), position.getY(), position.getZ()),
                breakStage
        ));
    }

    @Override
    public NMSPacket createSpawnEntityPacket(Entity entity) {
        PacketPlayOutSpawnEntity spawnEntity = new PacketPlayOutSpawnEntity(((CraftEntity) entity).getHandle(), 0);
        return new OutSpawnEntity(spawnEntity);
    }

    @Override
    public NMSPacket createSpawnEntityLivingPacket(Entity entity) {
        PacketPlayOutSpawnEntity spawnEntity = new PacketPlayOutSpawnEntity(((CraftLivingEntity) entity).getHandle());
        return new OutSpawnEntityLiving(spawnEntity);
    }

    @Override
    public NMSPacket createUpdateEntityMetadataPacket(Entity entity) {
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getEntityId(),
                ((CraftEntity) entity).getHandle().ai(),
                false);
        return new OutEntityMetadata(metadata);
    }

    @Override
    public NMSPacket createEntityEffectPacket(LivingEntity entity, PotionEffect effect) {
        MobEffect me = new MobEffect(MobEffectList.a(effect.getType().getId()), effect.getDuration(), effect.getAmplifier(), effect.isAmbient(), effect.hasParticles());

        PacketPlayOutEntityEffect entityEffect = new PacketPlayOutEntityEffect(entity.getEntityId(), me);
        return new OutEntityEffect(entityEffect);
    }

    @Override
    public NMSPacket createRemoveEntityEffectPacket(LivingEntity entity, PotionEffectType effect) {
        PacketPlayOutRemoveEntityEffect entityEffect = new PacketPlayOutRemoveEntityEffect(entity.getEntityId(), MobEffectList.a(effect.getId()));
        return new OutRemoveEntityEffect(entityEffect);
    }

    @Override
    public void sendPacket(Player player, NMSPacket packet) {
        ((CraftPlayer) player).getHandle().b.a((Packet) packet.getHandle());
    }
}
