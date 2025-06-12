package com.golfing8.kcommon.struct.entity;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.NMS;
import com.golfing8.kcommon.config.ConfigEntry;
import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.adapter.CASerializable;
import com.golfing8.kcommon.struct.item.ItemStackBuilder;
import com.golfing8.kcommon.util.RandomUtil;
import lombok.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wraps a living entity's equipment (armor/weapon)
 */
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class EntityEquipment implements CASerializable {

    static {
        Bukkit.getServer().getPluginManager().registerEvents(new Listener(), KCommon.getInstance());
    }

    /** The entity's helmet */
    private EntityEquipmentPiece helmet;
    /** The entity's chestplate */
    private EntityEquipmentPiece chestplate;
    /** The entity's leggings */
    private EntityEquipmentPiece leggings;
    /** The entity's boots */
    private EntityEquipmentPiece boots;
    /** The entity's item in hand */
    private EntityEquipmentPiece hand;
    /** The entity's off-hand item */
    private EntityEquipmentPiece offHand;

    /**
     * Applies the equipment to the given entity.
     *
     * @param entity the entity to apply the equipment to.
     */
    public void apply(LivingEntity entity) {
        org.bukkit.inventory.EntityEquipment equipment = entity.getEquipment();
        if (helmet != null && RandomUtil.testChance(helmet.chanceToEquip)) {
            equipment.setHelmet(helmet.buildBukkitStack(entity));
            equipment.setHelmetDropChance(-(float) helmet.getChanceToDrop() / 100);
        }
        if (chestplate != null && RandomUtil.testChance(chestplate.chanceToEquip)) {
            equipment.setChestplate(chestplate.buildBukkitStack(entity));
            equipment.setChestplateDropChance(-(float) chestplate.getChanceToDrop() / 100);
        }
        if (leggings != null && RandomUtil.testChance(leggings.chanceToEquip)) {
            equipment.setLeggings(leggings.buildBukkitStack(entity));
            equipment.setLeggingsDropChance(-(float) leggings.getChanceToDrop() / 100);
        }
        if (boots != null && RandomUtil.testChance(boots.chanceToEquip)) {
            equipment.setBoots(boots.buildBukkitStack(entity));
            equipment.setBootsDropChance(-(float) boots.getChanceToDrop() / 100);
        }
        if (hand != null && RandomUtil.testChance(hand.chanceToEquip)) {
            equipment.setItemInHand(hand.buildBukkitStack(entity));
            equipment.setItemInHandDropChance(-(float) hand.getChanceToDrop() / 100);
        }
        if (offHand != null && RandomUtil.testChance(offHand.chanceToEquip)) {
            NMS.getTheNMS().getMagicEntities().setItemInOffHand(entity, offHand.buildBukkitStack(entity));
            NMS.getTheNMS().getMagicEntities().setItemInOffHandDropChance(entity, -(float) offHand.getChanceToDrop() / 100);
        }
    }

    /**
     * Constructs an instance of this class from the given config section.
     *
     * @param section the section.
     * @return the equipment.
     */
    public static EntityEquipment fromSection(ConfigurationSection section) {
        EntityEquipmentPiece helmet = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "helmet"), EntityEquipmentPiece.class);
        EntityEquipmentPiece chestplate = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "chestplate"), EntityEquipmentPiece.class);
        EntityEquipmentPiece leggings = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "leggings"), EntityEquipmentPiece.class);
        EntityEquipmentPiece boots = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "boots"), EntityEquipmentPiece.class);
        EntityEquipmentPiece hand = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "hand"), EntityEquipmentPiece.class);
        EntityEquipmentPiece offHand = ConfigTypeRegistry.getFromType(new ConfigEntry(section, "off-hand"), EntityEquipmentPiece.class);

        return new EntityEquipment(helmet, chestplate, leggings, boots, hand, offHand);
    }

    @Getter
    @ToString
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class EntityEquipmentPiece implements CASerializable {
        private ItemStackBuilder piece;
        private double chanceToEquip = 100.0D;
        private double chanceToDrop;

        public ItemStack buildBukkitStack(LivingEntity context) {
            return piece == null ? null : piece.buildFromTemplate();
        }
    }

    private static class Listener implements org.bukkit.event.Listener {
        private final Random random = new Random();
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDeath(EntityDeathEvent event) {
            org.bukkit.inventory.EntityEquipment equipment = event.getEntity().getEquipment();
            // Interpret negative values as being what we should drop
            if (equipment.getHelmetDropChance() < 0.0F && equipment.getHelmet() != null) {
                float realChance = -equipment.getHelmetDropChance();
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), equipment.getHelmet());
            }

            if (equipment.getChestplateDropChance() < 0.0F && equipment.getChestplate() != null) {
                float realChance = -equipment.getChestplateDropChance();
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), equipment.getChestplate());
            }

            if (equipment.getLeggingsDropChance() < 0.0F && equipment.getLeggings() != null) {
                float realChance = -equipment.getLeggingsDropChance();
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), equipment.getLeggings());
            }

            if (equipment.getBootsDropChance() < 0.0F && equipment.getBoots() != null) {
                float realChance = -equipment.getBootsDropChance();
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), equipment.getBoots());
            }

            if (equipment.getItemInHandDropChance() < 0.0F && equipment.getItemInHand() != null) {
                float realChance = -equipment.getItemInHandDropChance();
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(), equipment.getItemInHand());
            }

            if (NMS.getTheNMS().getMagicEntities().getItemInOffHandDropChance(event.getEntity()) < 0.0F &&
                    NMS.getTheNMS().getMagicEntities().getItemInOffHand(event.getEntity()) != null) {
                float realChance = -NMS.getTheNMS().getMagicEntities().getItemInOffHandDropChance(event.getEntity());
                if (realChance < random.nextFloat())
                    event.getEntity().getWorld().dropItem(event.getEntity().getLocation(),
                            NMS.getTheNMS().getMagicEntities().getItemInOffHand(event.getEntity()));
            }
        }
    }
}
