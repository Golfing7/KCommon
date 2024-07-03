package com.golfing8.kcommon.nms.v1_17.access;

import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.access.NMSMagicEntities;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.mojang.authlib.GameProfile;
import lombok.RequiredArgsConstructor;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.monster.EntityGiantZombie;
import net.minecraft.world.entity.monster.EntityGuardianElder;
import net.minecraft.world.entity.monster.EntitySkeletonWither;
import net.minecraft.world.entity.monster.EntitySlime;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.craftbukkit.v1_17_R1.CraftRegionAccessor;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;

@RequiredArgsConstructor
public class MagicEntitiesV1_17 implements NMSMagicEntities {
    private final Plugin plugin;

    @Override
    public GameProfile getGameProfile(Player player) {
        return ((CraftPlayer) player).getHandle().getProfile();
    }

    @Override
    public Giant spawnGiantWithAI(Location location) {
        EntityGiantZombie giantZombie = (EntityGiantZombie) ((CraftWorld) location.getWorld()).createEntity(location, EntityType.GIANT.getEntityClass());

        WorldServer ws = ((CraftWorld) location.getWorld()).getHandle();

        //Nothing we can do here.
        if(!WineSpigot.isWineSpigot()){
            ws.addEntity(giantZombie);

            return (Giant) giantZombie.getBukkitEntity();
        }

//        giantZombie.goalSelector.a(0, new PathfinderGoalFloat(giantZombie));
//        giantZombie.goalSelector.a(2, new PathfinderGoalMeleeAttack(giantZombie, EntityHuman.class, 1.0D, false));
//        giantZombie.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(giantZombie, 1.0D));
//        giantZombie.goalSelector.a(7, new PathfinderGoalRandomStroll(giantZombie, 1.0D));
//        giantZombie.goalSelector.a(8, new PathfinderGoalLookAtPlayer(giantZombie, EntityHuman.class, 8.0F));
//        giantZombie.goalSelector.a(8, new PathfinderGoalRandomLookaround(giantZombie));
//
//        giantZombie.goalSelector.a(4, new PathfinderGoalMeleeAttack(giantZombie, EntityIronGolem.class, 1.0D, true));
//        giantZombie.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(giantZombie, 1.0D, false));
//        giantZombie.targetSelector.a(1, new PathfinderGoalHurtByTarget(giantZombie, true, EntityPigZombie.class));
//        giantZombie.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(giantZombie, EntityHuman.class, true));

        ws.addEntity(giantZombie);

        return (Giant) giantZombie.getBukkitEntity();
    }

    public Slime spawnSlimeWithSize(Location location, int size, double health){
        EntitySlime entitySlime = new EntitySlime(EntityTypes.aD, ((CraftWorld) location.getWorld()).getHandle());

        entitySlime.setSize(Math.max(size, 1), false);

        entitySlime.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entitySlime);

        return (Slime) entitySlime.getBukkitEntity();
    }

    public Monster spawnWitherSkeleton(Location location){
        EntitySkeletonWither entitySkeleton = new EntitySkeletonWither(EntityTypes.ba, ((CraftWorld) location.getWorld()).getHandle());

        entitySkeleton.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entitySkeleton);

        return (Skeleton) entitySkeleton.getBukkitEntity();
    }

    public Guardian spawnElderGuardian(Location location){
        EntityGuardianElder entityGuardian = new EntityGuardianElder(EntityTypes.t, ((CraftWorld) location.getWorld()).getHandle());

        entityGuardian.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entityGuardian);

        return (Guardian) entityGuardian.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, EntityData data, boolean randomizeData) {
        return (T) ((CraftRegionAccessor) world).spawn(loc, data.getEntityType().getEntityClass(), randomizeData, (spawned) -> {
            if (data.isCreeperCharged())
                ((Creeper) spawned).setPowered(true);
        });
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz) {
        net.minecraft.world.entity.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (T) entity.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        net.minecraft.world.entity.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addEntity(entity, reason);

        return (T) entity.getBukkitEntity();
    }

    private Constructor<EntityFallingBlock> fallingBlockConstructor;

    @Override
    public void setLoadChunks(Entity entity, boolean value) {
        //Couldn't find any field to match the v1_8 equivalent :/
    }

    @Override
    public boolean canEntitySpawn(LivingEntity entity) {
        EntityLiving living = ((CraftLivingEntity) entity).getHandle();

        if(!(living instanceof EntityInsentient))
            return true;

        EntityInsentient insentient = (EntityInsentient) living;

        return insentient.getWorld().b(insentient.getEntityType().a(insentient.locX(), insentient.locY(), insentient.locZ())) &&  insentient.a(insentient.t) && insentient.a(insentient.t, EnumMobSpawn.c);
    }

    @Override
    public boolean canEntityFit(Entity entity, Location location) {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
        Location offset = location.clone().subtract(entity.getLocation());
        return nmsEntity.t.getCubes(nmsEntity, nmsEntity.getBoundingBox().d(offset.getX(), offset.getY(), offset.getZ()));
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(Attribute.valueOf(attribute.name()));
        if (instance != null)
            instance.setBaseValue(value);
    }

    @Override
    public void setPersists(Creature entity, boolean value) {
        ((CraftCreature) entity).getHandle().persist = value;
    }

    @Override
    public void setKiller(LivingEntity entity, Player killer) {
        entity.setKiller(killer);
    }

    @Override
    public void setNoClip(Entity entity, boolean value) {
        ((CraftEntity) entity).getHandle().P = value;
    }

    @Override
    public void setFromSpawner(Entity entity, boolean value) {

    }

    @Override
    public void setMobAI(Entity entity, boolean value) {
        ((EntityInsentient) ((CraftLivingEntity) entity).getHandle()).aware = value;
    }
}
