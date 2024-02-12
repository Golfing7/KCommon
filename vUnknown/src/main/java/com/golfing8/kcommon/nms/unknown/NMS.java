package com.golfing8.kcommon.nms.unknown;

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
import org.bukkit.plugin.Plugin;

public class NMS implements NMSAccess {
    private final Server server;
    private final WorldguardHook wgHook;
    private final WorldEditHook worldEditHook;

    private final MagicItems magicItems;
    private final MagicEntities magicEntities;
    private final MagicPackets magicPackets;
    private final MagicNumbers magicNumbers;
    private final MagicEvents magicEvents;

    public NMS(Plugin plugin){
        this.server = new Server();
        this.wgHook = new Worldguard();
        this.worldEditHook = new WorldEdit();

        this.magicEntities = new MagicEntities(wgHook);
        this.magicItems = new MagicItems();
        this.magicNumbers = new MagicNumbers();
        this.magicPackets = new MagicPackets();
        this.magicEvents = new MagicEvents();

        Bukkit.getServer().getPluginManager().registerEvents(new PreSpawnSpawnerAdapter(), plugin);
        Bukkit.getServer().getPluginManager().registerEvents(new NewArmorEquipListener(), plugin);
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
