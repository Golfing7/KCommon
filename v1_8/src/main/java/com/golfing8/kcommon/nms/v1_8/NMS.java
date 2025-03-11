package com.golfing8.kcommon.nms.v1_8;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.nms.WineSpigot;
import com.golfing8.kcommon.nms.access.*;
import com.golfing8.kcommon.nms.v1_8.access.*;
import com.golfing8.kcommon.nms.v1_8.block.BlockDispenserV1_8;
import com.golfing8.kcommon.nms.v1_8.event.ArmorEquipHandler;
import com.golfing8.kcommon.nms.v1_8.event.WineSpigotArmorEquipListener;
import com.golfing8.kcommon.nms.v1_8.packets.*;
import com.golfing8.kcommon.nms.v1_8.server.ServerV1_8;
import com.golfing8.kcommon.nms.v1_8.worldedit.WorldEditV1_8;
import com.golfing8.kcommon.nms.v1_8.worldguard.WorldguardV1_8;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.v1_8.world.WorldV1_8;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.v1_8.block.BlockV1_8;
import com.golfing8.kcommon.nms.v1_8.event.PreSpawnSpawnerAdapter;
import lombok.Getter;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

public class NMS implements NMSAccess {
    private final BukkitAudiences audiences;
    private final MagicBlocksV1_8 magicBlocksV1_8;
    private final MagicNumbersV1_8 magicNumbersV1_8;
    private final MagicPacketsV1_8 magicPacketsV1_8;
    private final MagicEntitiesV1_8 magicEntitiesV1_8;
    private final MagicItemsV1_8 magicItemsV1_8;
    private final MagicEventsV1_8 magicEventsV1_8;

    private final ServerV1_8 server;
    private final WorldguardHook worldguardHook;
    private final WorldEditHook worldEditHook;
    @Getter
    private final Plugin plugin;

    public NMS(Plugin plugin){
        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);

        this.magicBlocksV1_8 = new MagicBlocksV1_8();
        this.magicNumbersV1_8 = new MagicNumbersV1_8();
        this.magicPacketsV1_8 = new MagicPacketsV1_8();
        this.magicEntitiesV1_8 = new MagicEntitiesV1_8();
        this.magicItemsV1_8 = new MagicItemsV1_8();
        this.worldguardHook = Bukkit.getPluginManager().isPluginEnabled("WorldGuard") ? new WorldguardV1_8() : WorldguardHook.EMPTY;
        this.worldEditHook = Bukkit.getPluginManager().isPluginEnabled("WorldEdit") ? new WorldEditV1_8(this) : WorldEditHook.EMPTY;
        this.magicEventsV1_8 = new MagicEventsV1_8();

        this.server = new ServerV1_8();

        Bukkit.getServer().getPluginManager().registerEvents(new PreSpawnSpawnerAdapter(), plugin);
        if (WineSpigot.isWineSpigot()) {
            Bukkit.getServer().getPluginManager().registerEvents(new WineSpigotArmorEquipListener(), plugin);
        } else {
            new ArmorEquipHandler(plugin, Collections.emptyList());
        }
    }

    public WorldEditHook getWorldEditHook() {
        return worldEditHook;
    }

    @Override
    public void sendMiniMessage(CommandSender player, String string) {
        audiences.sender(player).sendMessage(ComponentUtils.toComponent(string));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, title);
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.createInventory(holder, type, title);
    }

    private Boolean supportsPersistentDataContainers;
    @Override
    public boolean supportsPersistentDataContainers() {
        if (supportsPersistentDataContainers == null) {
            try {
                Class.forName("org.bukkit.persistence.PersistentDataContainer");
                supportsPersistentDataContainers = true;
            } catch (Throwable thr) {
                supportsPersistentDataContainers = false;
            }
        }
        return supportsPersistentDataContainers;
    }

    @Override
    public OfflinePlayer getOfflinePlayerIfCached(String str) {
        UserCache userCache = MinecraftServer.getServer().getUserCache();
        return userCache.getProfile(str) == null ? null : Bukkit.getOfflinePlayer(str);
    }

    @Override
    public NMSServer getMinecraftServer() {
        return server;
    }

    @Override
    public NMSWorld getWorld(World world) {
        return new WorldV1_8(((CraftWorld) world).getHandle());
    }

    @Override
    public NMSMagicNumberAccess getMagicNumbers() {
        return magicNumbersV1_8;
    }

    @Override
    public NMSPacket wrapPacket(Object packet) {
        if(packet instanceof PacketPlayInUseEntity)
            return new InUseEntityV1_8((PacketPlayInUseEntity) packet);
        else if(packet instanceof PacketPlayInBlockPlace)
            return new InBlockPlaceV1_8((PacketPlayInBlockPlace) packet);
        else if(packet instanceof PacketPlayInBlockDig)
            return new InBlockDigV1_8((PacketPlayInBlockDig) packet);
        else if(packet instanceof PacketPlayInWindowClick)
            return new InWindowClickV1_8((PacketPlayInWindowClick) packet);
        else if(packet instanceof PacketPlayInSetCreativeSlot)
            return new InSetCreativeSlotV1_8((PacketPlayInSetCreativeSlot) packet);
        else if(packet instanceof PacketPlayOutEntityDestroy)
            return new OutEntityDestroyV1_8((PacketPlayOutEntityDestroy) packet);
        else if(packet instanceof PacketPlayOutEntityStatus)
            return new OutEntityStatusV1_8((PacketPlayOutEntityStatus) packet);
        else if(packet instanceof PacketPlayOutBlockBreakAnimation)
            return new OutBreakAnimationV1_8((PacketPlayOutBlockBreakAnimation) packet);
        else if(packet instanceof PacketPlayOutSpawnEntity)
            return new OutSpawnEntityV1_8((PacketPlayOutSpawnEntity) packet);
        else if(packet instanceof PacketPlayOutEntityMetadata)
            return new OutEntityMetadataV1_8((PacketPlayOutEntityMetadata) packet);
        else if(packet instanceof PacketPlayOutSpawnEntityLiving)
            return new OutSpawnEntityLivingV1_8((PacketPlayOutSpawnEntityLiving) packet);
        else if(packet instanceof PacketPlayOutMultiBlockChange)
            return new OutMultiBlockChangeV1_8((PacketPlayOutMultiBlockChange) packet);
        else if(packet instanceof PacketPlayOutBlockChange)
            return new OutBlockChangeV1_8((PacketPlayOutBlockChange) packet);
        else if(packet instanceof PacketPlayOutEntityEffect)
            return new OutEntityEffectV1_8((PacketPlayOutEntityEffect) packet);
        else if(packet instanceof PacketPlayOutRemoveEntityEffect)
            return new OutRemoveEntityEffectV1_8((PacketPlayOutRemoveEntityEffect) packet);
        return null;
    }

    @Override
    public NMSBlock getBlock(Material material) {
        switch (material){
            case DISPENSER:
                return new BlockDispenserV1_8((BlockDispenser) Blocks.DISPENSER);
        }
        return new BlockV1_8(CraftMagicNumbers.getBlock(material));
    }

    @Override
    public NMSMagicBlocks getMagicBlocks() {
        return magicBlocksV1_8;
    }

    @Override
    public NMSMagicEntities getMagicEntities() {
        return magicEntitiesV1_8;
    }

    @Override
    public NMSMagicPackets getMagicPackets() {
        return magicPacketsV1_8;
    }

    @Override
    public NMSMagicItems getMagicItems() {
        return magicItemsV1_8;
    }

    @Override
    public NMSMagicEvents getMagicEvents() {
        return magicEventsV1_8;
    }

    @Override
    public WorldguardHook getWGHook() {
        return worldguardHook;
    }

    @Override
    public void teleportPlayerNoEvent(Player player, Location location) {
        if(player.getVehicle() != null)
            player.getVehicle().eject();

        WorldServer toWorld = ((CraftWorld) location.getWorld()).getHandle();

        WorldServer fromWorld = ((CraftWorld) player.getWorld()).getHandle();

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if(toWorld == fromWorld)
            entityPlayer.playerConnection.teleport(location);
        else
            MinecraftServer.getServer().getPlayerList().moveToWorld(entityPlayer, toWorld.dimension, true, location, true);
    }

    @Override
    public void sendActionBar(Player player, String string) {
        PacketPlayOutChat chat = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + string + "\"}"), (byte) 2);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(chat);
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        PacketPlayOutTitle packetOutTitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE,
                IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + title + "\"}"), fadeInTime, showTime, fadeOutTime);
        PacketPlayOutTitle packetOutSubtitle = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE,
                IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + subtitle + "\"}"), fadeInTime, showTime, fadeOutTime);
        PacketPlayOutTitle packetOutTimes = new PacketPlayOutTitle(fadeInTime, showTime, fadeOutTime);

        if (title != null) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetOutTitle);
        }
        if (subtitle != null) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetOutSubtitle);
        }
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetOutTimes);
    }

    @Override
    public long getCurrentTick() {
        return MinecraftServer.currentTick;
    }
}
