package com.golfing8.kcommon.nms.v1_17;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.nms.access.*;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.block.NMSBlockData;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.v1_17.access.*;
import com.golfing8.kcommon.nms.v1_17.block.BlockDataV1_17;
import com.golfing8.kcommon.nms.v1_17.block.BlockDispenserV1_17;
import com.golfing8.kcommon.nms.v1_17.block.BlockV1_17;
import com.golfing8.kcommon.nms.v1_17.event.NewArmorEquipListener;
import com.golfing8.kcommon.nms.v1_17.event.PreSpawnSpawnerAdapter;
import com.golfing8.kcommon.nms.v1_17.packets.*;
import com.golfing8.kcommon.nms.v1_17.server.ServerV1_17;
import com.golfing8.kcommon.nms.v1_17.world.WorldV1_17;
import com.golfing8.kcommon.nms.v1_17.worldedit.WorldEdit;
import com.golfing8.kcommon.nms.v1_17.worldguard.WorldguardV1_17;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.BlockDispenser;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class NMS implements NMSAccess {
    private final BukkitAudiences audiences;
    private final Plugin plugin;
    private final ServerV1_17 server;
    private final WorldguardHook wgHook;
    private final WorldEditHook worldEditHook;
    private final MagicItemsV1_17 magicItemsV1_17;
    private final MagicEntitiesV1_17 magicEntitiesV1_17;
    private final MagicPacketsV1_17 magicPacketsV1_17;
    private final MagicNumbersV1_17 magicNumbersV1_17;
    private final MagicEventsV1_17 magicEventsV1_17;

    public NMS(Plugin plugin){
        this.server = new ServerV1_17();
        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);

        this.magicItemsV1_17 = new MagicItemsV1_17();
        this.magicEntitiesV1_17 = new MagicEntitiesV1_17(plugin);
        this.magicPacketsV1_17 = new MagicPacketsV1_17();
        this.magicNumbersV1_17 = new MagicNumbersV1_17();
        this.magicEventsV1_17 = new MagicEventsV1_17();
        this.wgHook = Bukkit.getPluginManager().isPluginEnabled("WorldGuard") ? new WorldguardV1_17() : WorldguardHook.EMPTY;
        this.worldEditHook = Bukkit.getPluginManager().isPluginEnabled("WorldEdit") ? new WorldEdit() : WorldEditHook.EMPTY;

        Bukkit.getServer().getPluginManager().registerEvents(new PreSpawnSpawnerAdapter(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new NewArmorEquipListener(), plugin);
    }

    @Override
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

    @Override
    public boolean supportsPersistentDataContainers() {
        return true;
    }

    @Override
    public OfflinePlayer getOfflinePlayerIfCached(String str) {
        return Bukkit.getOfflinePlayerIfCached(str);
    }

    @Override
    public NMSServer getMinecraftServer() {
        return server;
    }

    @Override
    public NMSWorld getWorld(World world) {
        return new WorldV1_17(((CraftWorld) world).getHandle());
    }

    @Override
    public NMSMagicNumberAccess getMagicNumbers() {
        return magicNumbersV1_17;
    }

    @Override
    public NMSPacket wrapPacket(Object packet) {
        if(packet instanceof PacketPlayInUseEntity)
            return new InUseEntityV1_17((PacketPlayInUseEntity) packet);
        else if(packet instanceof PacketPlayInUseItem)
            return new InUseItemV1_17((PacketPlayInUseItem) packet);
        else if(packet instanceof PacketPlayInBlockPlace)
            return new InBlockPlaceV1_17((PacketPlayInBlockPlace) packet);
        else if(packet instanceof PacketPlayInBlockDig)
            return new InBlockDigV1_17((PacketPlayInBlockDig) packet);
        else if(packet instanceof PacketPlayInWindowClick)
            return new InWindowClickV1_17((PacketPlayInWindowClick) packet);
        else if(packet instanceof PacketPlayInSetCreativeSlot)
            return new InSetCreativeSlotV1_17(
                    (PacketPlayInSetCreativeSlot) packet);
        else if(packet instanceof PacketPlayOutEntityDestroy)
            return new OutEntityDestroyV1_17((PacketPlayOutEntityDestroy) packet);
        else if(packet instanceof PacketPlayOutEntityStatus)
            return new OutEntityStatusV1_17((PacketPlayOutEntityStatus) packet);
        else if(packet instanceof PacketPlayOutBlockBreakAnimation)
            return new OutBreakAnimationV1_17((PacketPlayOutBlockBreakAnimation) packet);
        else if(packet instanceof PacketPlayOutSpawnEntity)
            return new OutSpawnEntityV1_17((PacketPlayOutSpawnEntity) packet);
        else if(packet instanceof PacketPlayOutEntityMetadata)
            return new OutEntityMetadataV1_17((PacketPlayOutEntityMetadata) packet);
        else if(packet instanceof PacketPlayOutSpawnEntityLiving)
            return new OutSpawnEntityLivingV1_17((PacketPlayOutSpawnEntityLiving) packet);
        else if(packet instanceof PacketPlayOutMultiBlockChange)
            return new OutMultiBlockChangeV1_17((PacketPlayOutMultiBlockChange) packet);
        else if(packet instanceof PacketPlayOutBlockChange)
            return new OutBlockChangeV1_17((PacketPlayOutBlockChange) packet);
        else if(packet instanceof PacketPlayOutEntityEffect)
            return new OutEntityEffectV1_17((PacketPlayOutEntityEffect) packet);
        else if(packet instanceof PacketPlayOutRemoveEntityEffect)
            return new OutRemoveEntityEffectV1_17((PacketPlayOutRemoveEntityEffect) packet);
        return null;
    }

    @Override
    public NMSBlock getBlock(Material material) {
        switch (material){
            case DISPENSER:
                return new BlockDispenserV1_17((BlockDispenser) Blocks.ay);
        }
        return new BlockV1_17(CraftMagicNumbers.getBlock(material));
    }

    @Override
    public NMSMagicEntities getMagicEntities() {
        return magicEntitiesV1_17;
    }

    @Override
    public NMSMagicPackets getMagicPackets() {
        return magicPacketsV1_17;
    }

    @Override
    public NMSMagicItems getMagicItems() {
        return magicItemsV1_17;
    }

    @Override
    public WorldguardHook getWGHook() {
        return wgHook;
    }

    @Override
    public NMSMagicEvents getMagicEvents() {
        return magicEventsV1_17;
    }

    @Override
    public void teleportPlayerNoEvent(Player player, Location location) {
        if(player.getVehicle() != null)
            player.getVehicle().eject();

        WorldServer toWorld = ((CraftWorld) location.getWorld()).getHandle();

        WorldServer fromWorld = ((CraftWorld) player.getWorld()).getHandle();

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if(toWorld == fromWorld)
            entityPlayer.b.teleport(location);
        else
            MinecraftServer.getServer().getPlayerList().moveToWorld(entityPlayer, toWorld, true, location, true);
    }

    @Override
    public void sendActionBar(Player player, String string) {
        player.sendActionBar(Component.text(string));
    }

    @Override
    public void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime) {
        player.sendTitle(title, subtitle, fadeInTime, showTime, fadeOutTime);
    }

    @Override
    public long getCurrentTick() {
        return MinecraftServer.currentTick;
    }
}
