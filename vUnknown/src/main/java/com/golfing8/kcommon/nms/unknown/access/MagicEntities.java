package com.golfing8.kcommon.nms.unknown.access;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.golfing8.kcommon.nms.access.NMSMagicEntities;
import com.golfing8.kcommon.nms.struct.EntityAttribute;
import com.golfing8.kcommon.nms.struct.EntityAttributeModifier;
import com.golfing8.kcommon.nms.struct.EntityData;
import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

import java.util.function.Consumer;

/**
 * API agnostic entity access
 */
public class MagicEntities implements NMSMagicEntities {

    @Override
    public GameProfile getGameProfile(Player player) {
        PlayerProfile profile = player.getPlayerProfile();
        return new GameProfile(profile.getId(), profile.getName());
    }

    @Override
    public Giant spawnGiantWithAI(Location location) {
        return location.getWorld().spawn(location, Giant.class);
    }

    @Override
    public Slime spawnSlimeWithSize(Location location, int size, double health) {
        Slime slime = location.getWorld().spawn(location, Slime.class);
        slime.setSize(size);
        slime.setHealth(health);
        return slime;
    }

    @Override
    public Monster spawnWitherSkeleton(Location location) {
        return location.getWorld().spawn(location, WitherSkeleton.class);
    }

    @Override
    public Guardian spawnElderGuardian(Location location) {
        return location.getWorld().spawn(location, ElderGuardian.class);
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, EntityData data, boolean randomizeData) {
        return (T) world.spawn(loc, data.getEntityType().getEntityClass(), randomizeData, spawned -> {
            if (data.isCreeperCharged())
                ((Creeper) spawned).setPowered(true);
        });
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz) {
        return loc.getWorld().spawn(loc, clazz);
    }

    @Override
    public <T extends Entity> T spawnEntity(World world, Location loc, Class<T> clazz, CreatureSpawnEvent.SpawnReason reason) {
        return loc.getWorld().spawn(loc, clazz, reason);
    }

    @Override
    public <T extends Entity> T spawn(Location location, Class<T> clazz, Consumer<? super T> spawnConsumer) {
        return location.getWorld().spawn(location, clazz, spawnConsumer);
    }

    @Override
    public void setLoadChunks(Entity entity, boolean value) {
        //Couldn't find any field to match the v1_8 equivalent :/
    }

    @Override
    public boolean canEntitySpawn(LivingEntity entity) {
        return canEntityFit(entity);
    }

    @Override
    public boolean canEntityFit(Entity entity, Location location) {
        BoundingBox box = entity.getBoundingBox().shift(location.clone().subtract(entity.getLocation()));
        int minX = (int) Math.floor(box.getMinX());
        int minY = (int) Math.max(Math.floor(box.getMinY()) - 1, entity.getWorld().getMinHeight());
        int minZ = (int) Math.floor(box.getMinZ());
        int maxX = (int) Math.floor(box.getMaxX());
        int maxY = (int) Math.min(Math.floor(box.getMaxY()), entity.getWorld().getMaxHeight());
        int maxZ = (int) Math.floor(box.getMaxZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = entity.getWorld().getBlockAt(x, y, z);
                    VoxelShape blockShape = block.getCollisionShape();
                    if (blockShape.overlaps(box))
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Translates the KCommon EntityAttribute to a bukkit attribute
     *
     * @param entityAttribute the entity attribute
     * @return the attribute
     */
    public static Attribute translateAttribute(EntityAttribute entityAttribute) {
        Attribute instance = null;
        switch (entityAttribute) {
            case GENERIC_ATTACK_DAMAGE:
                instance = getAttributeWithNameContaining("attack_damage");
                break;
            case GENERIC_MOVEMENT_SPEED:
                instance = getAttributeWithNameContaining("movement_speed");
                break;
            case GENERIC_KNOCKBACK_RESISTANCE:
                instance = getAttributeWithNameContaining("knockback_resistance");
                break;
            case GENERIC_MAX_HEALTH:
                instance = getAttributeWithNameContaining("max_health");
                break;
            case GENERIC_LUCK:
                instance = getAttributeWithNameContaining("luck");
                break;
            case GENERIC_ARMOR:
                instance = getAttributeWithNameEnding("armor");
                break;
            case GENERIC_ATTACK_SPEED:
                instance = getAttributeWithNameContaining("attack_speed");
                break;
            case GENERIC_FLYING_SPEED:
                instance = getAttributeWithNameContaining("flying_speed");
                break;
            case GENERIC_FOLLOW_RANGE:
                instance = getAttributeWithNameContaining("follow_range");
                break;
            case GENERIC_ARMOR_TOUGHNESS:
                instance = getAttributeWithNameContaining("armor_toughness");
                break;
            case GENERIC_ATTACK_KNOCKBACK:
                instance = getAttributeWithNameContaining("attack_knockback");
                break;
            case HORSE_JUMP_STRENGTH:
                instance = getAttributeWithNameContaining("jump_strength");
                break;
            case ZOMBIE_SPAWN_REINFORCEMENTS:
                instance = getAttributeWithNameContaining("spawn_reinforcements");
                break;
            case GRAVITY:
                instance = getAttributeWithNameContaining("gravity");
                break;
            default:
                for (String name : entityAttribute.getOldNames()) {
                    instance = getAttributeWithNameContaining(name);
                    if (instance != null)
                        break;
                }
                break;
        }
        return instance;
    }

    private static Attribute getAttributeWithNameContaining(String key) {
        String lcKey = key.toLowerCase();
        return Registry.ATTRIBUTE.stream().filter(attr -> attr.getKey().getKey().toLowerCase().contains(lcKey)).findFirst().orElse(null);
    }

    private static Attribute getAttributeWithNameEnding(String key) {
        String lcKey = key.toLowerCase();
        return Registry.ATTRIBUTE.stream().filter(attr -> attr.getKey().getKey().endsWith(lcKey)).findFirst().orElse(null);
    }

    @Override
    public void setAttribute(LivingEntity entity, EntityAttribute attribute, double value) {
        AttributeInstance instance = entity.getAttribute(translateAttribute(attribute));

        if (instance != null)
            instance.setBaseValue(value);
    }

    @Override
    public void addAttributeModifier(LivingEntity entity, EntityAttribute attribute, EntityAttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(translateAttribute(attribute));

        if (instance != null) {
            AttributeModifier attributeModifier = new AttributeModifier(
                    NamespacedKey.fromString(modifier.getUuid().toString()),
                    modifier.getAmount(),
                    AttributeModifier.Operation.valueOf(modifier.getOperation().name()),
                    modifier.getSlot() == null ? EquipmentSlotGroup.ANY : modifier.getSlot().getGroup()
            );
            instance.removeModifier(attributeModifier);
            instance.addTransientModifier(attributeModifier);
        }
    }

    @Override
    public void removeAttributeModifier(LivingEntity entity, EntityAttribute attribute, EntityAttributeModifier modifier) {
        AttributeInstance instance = entity.getAttribute(translateAttribute(attribute));

        if (instance != null) {
            AttributeModifier attributeModifier = new AttributeModifier(
                    NamespacedKey.fromString(modifier.getUuid().toString()),
                    modifier.getAmount(),
                    AttributeModifier.Operation.valueOf(modifier.getOperation().name()),
                    modifier.getSlot() == null ? EquipmentSlotGroup.ANY : modifier.getSlot().getGroup()
            );
            instance.removeModifier(attributeModifier);
        }
    }

    @Override
    public void setPersists(Creature entity, boolean value) {
        entity.setPersistent(value);
    }

    @Override
    public void setKiller(LivingEntity entity, Player killer) {
        entity.setKiller(killer);
    }

    @Override
    public void setNoClip(Entity entity, boolean value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setFromSpawner(Entity entity, boolean value) {
        setMobAI(entity, !value);
    }

    @Override
    public void setMobAI(Entity entity, boolean value) {
        Mob mob = (Mob) entity;
        mob.setAware(value);
    }

    @Override
    public void setGravity(Entity entity, boolean value) {
        entity.setGravity(value);
    }
}
