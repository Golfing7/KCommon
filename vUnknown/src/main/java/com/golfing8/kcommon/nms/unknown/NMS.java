package com.golfing8.kcommon.nms.unknown;

import com.golfing8.kcommon.ComponentUtils;
import com.golfing8.kcommon.nms.access.*;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.unknown.access.*;
import com.golfing8.kcommon.nms.unknown.block.Block;
import com.golfing8.kcommon.nms.unknown.event.NewArmorEquipListener;
import com.golfing8.kcommon.nms.unknown.event.PreSpawnSpawnerAdapter;
import com.golfing8.kcommon.nms.unknown.server.Server;
import com.golfing8.kcommon.nms.unknown.world.World;
import com.golfing8.kcommon.nms.unknown.worldedit.WorldEdit;
import com.golfing8.kcommon.nms.unknown.worldguard.Worldguard;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

public class NMS implements NMSAccess {
    private final Server server;
    private final WorldguardHook wgHook;
    private final WorldEditHook worldEditHook;

    private final MagicBlocks magicBlocks;
    private final MagicItems magicItems;
    private final MagicEntities magicEntities;
    private final MagicPackets magicPackets;
    private final MagicNumbers magicNumbers;
    private final MagicEvents magicEvents;
    private final MagicInventories magicInventories;

    public NMS(Plugin plugin){
        this.server = new Server();

        this.magicBlocks = new MagicBlocks();
        this.magicEntities = new MagicEntities();
        this.magicItems = new MagicItems();
        this.magicNumbers = new MagicNumbers();
        this.magicPackets = new MagicPackets();
        this.magicEvents = new MagicEvents();
        this.magicInventories = new MagicInventories();
        this.wgHook = Bukkit.getPluginManager().isPluginEnabled("WorldGuard") ? new Worldguard() : WorldguardHook.EMPTY;
        this.worldEditHook = Bukkit.getPluginManager().isPluginEnabled("WorldEdit") ? new WorldEdit() : WorldEditHook.EMPTY;

        Bukkit.getServer().getPluginManager().registerEvents(new PreSpawnSpawnerAdapter(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new NewArmorEquipListener(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(this.magicInventories, plugin);
    }

    @Override
    public WorldEditHook getWorldEditHook() {
        return worldEditHook;
    }

    @Override
    public void sendMiniMessage(CommandSender player, String string) {
        player.sendMessage(ComponentUtils.toComponent(string));
    }

    @Override
    public void broadcastComponent(Component component) {
        Bukkit.broadcast(component);
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, int size, String title) {
        return Bukkit.createInventory(holder, size, ComponentUtils.toComponent(title));
    }

    @Override
    public Inventory createInventory(InventoryHolder holder, InventoryType type, String title) {
        return Bukkit.createInventory(holder, type, ComponentUtils.toComponent(title));
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
    public NMSWorld getWorld(org.bukkit.World world) {
        return new World(world);
    }

    @Override
    public NMSMagicNumberAccess getMagicNumbers() {
        return magicNumbers;
    }

    @Override
    public NMSPacket wrapPacket(Object packet) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSBlock getBlock(Material material) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NMSMagicBlocks getMagicBlocks() {
        return magicBlocks;
    }

    @Override
    public NMSMagicEntities getMagicEntities() {
        return magicEntities;
    }

    @Override
    public NMSMagicPackets getMagicPackets() {
        return magicPackets;
    }

    @Override
    public NMSMagicItems getMagicItems() {
        return magicItems;
    }

    @Override
    public NMSMagicEvents getMagicEvents() {
        return magicEvents;
    }

    @Override
    public NMSMagicInventories getMagicInventories() {
        return magicInventories;
    }

    @Override
    public WorldguardHook getWGHook() {
        return wgHook;
    }

    @Override
    public void teleportPlayerNoEvent(Player player, Location location) {
        throw new UnsupportedOperationException();
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
        return Bukkit.getServer().getCurrentTick();
    }
}
