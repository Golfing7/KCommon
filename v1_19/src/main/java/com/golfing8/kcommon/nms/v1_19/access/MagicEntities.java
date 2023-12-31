package com.golfing8.kcommon.nms.v1_19.access;

import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.golfing8.kcommon.nms.access.NMSMagicEntities;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.mojang.authlib.GameProfile;
import lombok.AllArgsConstructor;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.EntityFallingBlock;
import net.minecraft.world.entity.monster.EntityGiantZombie;
import net.minecraft.world.entity.monster.EntityGuardianElder;
import net.minecraft.world.entity.monster.EntitySkeletonWither;
import net.minecraft.world.entity.monster.EntitySlime;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MagicEntities implements NMSMagicEntities {
    private final WorldguardHook hook;

    private final FieldHandle<IBlockData> fallingBlockBlockData = (FieldHandle<IBlockData>) FieldHandles.getHandle("ao", EntityFallingBlock.class);

    @Override
    public GameProfile getGameProfile(Player player) {
        return ((CraftPlayer) player).getHandle().fy();
    }

    @Override
    public Giant spawnGiantWithAI(Location location) {
        EntityGiantZombie giantZombie = (EntityGiantZombie) ((CraftWorld) location.getWorld()).createEntity(location, EntityType.GIANT.getEntityClass());

        WorldServer ws = ((CraftWorld) location.getWorld()).getHandle();

        //Nothing we can do here.
        if(!WineSpigot.isWineSpigot()){
            ws.addFreshEntity(giantZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);

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

        ws.addFreshEntity(giantZombie, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (Giant) giantZombie.getBukkitEntity();
    }

    public Slime spawnSlimeWithSize(Location location, int size, double health){
        EntitySlime entitySlime = new EntitySlime(EntityTypes.aG, ((CraftWorld) location.getWorld()).getHandle());

        entitySlime.a(Math.max(size, 1), false);

        entitySlime.o(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(entitySlime, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (Slime) entitySlime.getBukkitEntity();
    }

    public Monster spawnWitherSkeleton(Location location){
        EntitySkeletonWither entitySkeleton = new EntitySkeletonWither(EntityTypes.bf, ((CraftWorld) location.getWorld()).getHandle());

        entitySkeleton.o(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(entitySkeleton, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (Skeleton) entitySkeleton.getBukkitEntity();
    }

    public Guardian spawnElderGuardian(Location location){
        EntityGuardianElder entityGuardian = new EntityGuardianElder(EntityTypes.v, ((CraftWorld) location.getWorld()).getHandle());

        entityGuardian.o(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addFreshEntity(entityGuardian, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (Guardian) entityGuardian.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T createEntity(World world, Location loc, Class<T> clazz) {
        return (T) ((CraftWorld) world).createEntity(loc, clazz).getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz) {
        net.minecraft.world.entity.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addFreshEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (T) entity.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        net.minecraft.world.entity.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addFreshEntity(entity, reason);

        return (T) entity.getBukkitEntity();
    }

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

        return insentient.s.b(insentient.ad().a(insentient.df(), insentient.dh(), insentient.dl())) && insentient.a(insentient.s) && insentient.a(insentient.s, EnumMobSpawn.c);
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        switch(attribute){
            case ATTACK_DAMAGE:
                entity.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(value);
                break;
            case MOVEMENT_SPEED:
                entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
                break;
            case KB_RESISTANCE:
                entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE).setBaseValue(value);
                break;
        }
    }

    @Override
    public void setPersists(Creature entity, boolean value) {
        ((CraftCreature) entity).getHandle().persist = value;
    }

    @Override
    public void setNoClip(Entity entity, boolean value) {
        ((CraftEntity) entity).getHandle().Q = value;
    }

    @Override
    public void setFromSpawner(Entity entity, boolean value) {
        setMobAI(entity, !value);
    }

    @Override
    public void setMobAI(Entity entity, boolean value) {
        ((EntityInsentient) ((CraftLivingEntity) entity).getHandle()).aware = value;
    }
}
