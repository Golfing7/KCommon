package com.golfing8.kcommon.nms.v1_8.access;

import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.golfing8.kcommon.nms.access.NMSMagicEntities;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.mojang.authlib.GameProfile;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTEntity;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import lombok.AllArgsConstructor;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

@AllArgsConstructor
public class MagicEntitiesV1_8 implements NMSMagicEntities {
    private final WorldguardHook hook;

    @Override
    public GameProfile getGameProfile(Player player) {
        return ((CraftPlayer) player).getHandle().getProfile();
    }

    @Override
    public Giant spawnGiantWithAI(Location location) {
        EntityGiantZombie giantZombie = (EntityGiantZombie) ((CraftWorld) location.getWorld()).createEntity(location, EntityType.GIANT.getEntityClass());

        giantZombie.loadChunks = true;

        WorldServer ws = ((CraftWorld) location.getWorld()).getHandle();

        //Nothing we can do here.
        if(!WineSpigot.isWineSpigot()){
            ws.addEntity(giantZombie);

            return (Giant) giantZombie.getBukkitEntity();
        }

        giantZombie.goalSelector.a(0, new PathfinderGoalFloat(giantZombie));
        giantZombie.goalSelector.a(2, new PathfinderGoalMeleeAttack(giantZombie, EntityHuman.class, 1.0D, false));
        giantZombie.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(giantZombie, 1.0D));
        giantZombie.goalSelector.a(7, new PathfinderGoalRandomStroll(giantZombie, 1.0D));
        giantZombie.goalSelector.a(8, new PathfinderGoalLookAtPlayer(giantZombie, EntityHuman.class, 8.0F));
        giantZombie.goalSelector.a(8, new PathfinderGoalRandomLookaround(giantZombie));

        giantZombie.goalSelector.a(4, new PathfinderGoalMeleeAttack(giantZombie, EntityIronGolem.class, 1.0D, true));
        giantZombie.goalSelector.a(6, new PathfinderGoalMoveThroughVillage(giantZombie, 1.0D, false));
        giantZombie.targetSelector.a(1, new PathfinderGoalHurtByTarget(giantZombie, true, EntityPigZombie.class));
        giantZombie.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(giantZombie, EntityHuman.class, true));

        ws.addEntity(giantZombie);

        return (Giant) giantZombie.getBukkitEntity();
    }

    public Slime spawnSlimeWithSize(Location location, int size, double health){
        EntitySlime entitySlime = new EntitySlime(((CraftWorld) location.getWorld()).getHandle());

        entitySlime.loadChunks = true;

        entitySlime.setSize(Math.max(size, 1));

        entitySlime.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entitySlime);

        return (Slime) entitySlime.getBukkitEntity();
    }

    public Monster spawnWitherSkeleton(Location location){
        EntitySkeleton entitySkeleton = new EntitySkeleton(((CraftWorld) location.getWorld()).getHandle());

        entitySkeleton.loadChunks = true;

        entitySkeleton.setSkeletonType(Skeleton.SkeletonType.WITHER.getId());

        entitySkeleton.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entitySkeleton);

        return (Skeleton) entitySkeleton.getBukkitEntity();
    }

    public Guardian spawnElderGuardian(Location location){
        EntityGuardian entityGuardian = new EntityGuardian(((CraftWorld) location.getWorld()).getHandle());

        entityGuardian.loadChunks = true;

        entityGuardian.setElder(true);

        entityGuardian.setPosition(location.getX(), location.getY(), location.getZ());

        ((CraftWorld) location.getWorld()).getHandle().addEntity(entityGuardian);

        return (Guardian) entityGuardian.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, EntityData entityData) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftWorld) world).createEntity(loc, entityData.getEntityType().getEntityClass());
        if (entityData.isCreeperCharged()) {
            ((EntityCreeper) entity).setPowered(true);
        }

        if (entityData.isHorseSkeleton()) {
            ((EntityHorse) entity).setType(4);
        }

        if (entityData.isHorseUndead()) {
            ((EntityHorse) entity).setType(3);
        }

        if (entityData.isSkeletonWither()) {
            ((EntitySkeleton) entity).setSkeletonType(1);
        }
        ((CraftWorld) world).addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);
        return (T) entity.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addEntity(entity, CreatureSpawnEvent.SpawnReason.CUSTOM);

        return (T) entity.getBukkitEntity();
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        net.minecraft.server.v1_8_R3.Entity entity = ((CraftWorld) world).createEntity(loc, clazz);

        ((CraftWorld) world).getHandle().addEntity(entity, reason);

        return (T) entity.getBukkitEntity();
    }

    @Override
    public void setLoadChunks(Entity entity, boolean value) {
        ((CraftEntity) entity).getHandle().loadChunks = value;
    }

    @Override
    public void setPersists(Creature entity, boolean value) {
        ((CraftCreature) entity).getHandle().persistent = value;
    }

    @Override
    public void setKiller(LivingEntity entity, Player killer) {
        ((CraftLivingEntity) entity).getHandle().killer = ((CraftPlayer) killer).getHandle();
    }

    @Override
    public boolean canEntitySpawn(LivingEntity entity) {
        EntityLiving living = ((CraftLivingEntity) entity).getHandle();

        if(!(living instanceof EntityInsentient))
            return true;

        return ((EntityInsentient) living).canSpawn();
    }

    @Override
    public void setItemInOffHand(LivingEntity entity, ItemStack stack) {

    }

    @Override
    public void setItemInOffHandDropChance(LivingEntity entity, float chance) {

    }

    @Override
    public ItemStack getItemInOffHand(LivingEntity entity) {
        return null;
    }

    @Override
    public float getItemInOffHandDropChance(LivingEntity entity) {
        return 0.0F;
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        NBTEntity nbtEntity = new NBTEntity(entity);

        NBTCompoundList nbtCompoundList = nbtEntity.getCompoundList("Attributes");

        //Not the best way, but I don't want to extract it out of the switch.
        for (ReadWriteNBT nbtListCompound : nbtCompoundList) {
            switch(nbtListCompound.getString("Name")){
                case "generic.attackDamage":
                    if(attribute == EntityAttribute.ATTACK_DAMAGE){
                        nbtListCompound.setDouble("Base", value);
                        return;
                    }
                    break;
                case "generic.knockbackResistance":
                    if(attribute == EntityAttribute.KB_RESISTANCE){
                        nbtListCompound.setDouble("Base", value);
                        return;
                    }
                    break;
                case "generic.movementSpeed":
                    if(attribute == EntityAttribute.MOVEMENT_SPEED){
                        nbtListCompound.setDouble("Base", value);
                        return;
                    }
                    break;
            }
        }
    }

    @Override
    public void setNoClip(Entity entity, boolean value) {
        ((CraftEntity) entity).getHandle().noclip = value;
    }

    @Override
    public void setFromSpawner(Entity entity, boolean value) {
        ((CraftEntity) entity).getHandle().fromMobSpawner = value;
    }

    @Override
    public void setMobAI(Entity entity, boolean value) {
        setFromSpawner(entity, !value);
    }
}
