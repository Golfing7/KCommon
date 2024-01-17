package com.golfing8.kcommon.nms.v1_19;

import com.golfing8.kcommon.nms.access.*;
import com.golfing8.kcommon.nms.v1_19.access.*;
import com.golfing8.kcommon.nms.v1_19.packets.*;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.v1_19.block.BlockDispenser;
import com.golfing8.kcommon.nms.v1_19.block.Block;
import com.golfing8.kcommon.nms.v1_19.event.NewArmorEquipListener;
import com.golfing8.kcommon.nms.v1_19.server.Server;
import com.golfing8.kcommon.nms.v1_19.world.World;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.golfing8.kcommon.nms.v1_19.event.PreSpawnSpawnerAdapter;
import com.golfing8.kcommon.nms.v1_19.worldedit.WorldEdit;
import com.golfing8.kcommon.nms.v1_19.worldguard.Worldguard;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
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
    public NMSServer getMinecraftServer() {
        return server;
    }

    @Override
    public NMSWorld getWorld(org.bukkit.World world) {
        return new World(((CraftWorld) world).getHandle());
    }

    @Override
    public NMSMagicNumberAccess getMagicNumbers() {
        return magicNumbers;
    }

    @Override
    public NMSPacket wrapPacket(Object packet) {
        if(packet instanceof PacketPlayInUseEntity)
            return new InUseEntity((PacketPlayInUseEntity) packet);
        else if(packet instanceof PacketPlayInUseItem)
            return new InUseItem((PacketPlayInUseItem) packet);
        else if(packet instanceof PacketPlayInBlockPlace)
            return new InBlockPlace((PacketPlayInBlockPlace) packet);
        else if(packet instanceof PacketPlayInBlockDig)
            return new InBlockDig((PacketPlayInBlockDig) packet);
        else if(packet instanceof PacketPlayInWindowClick)
            return new InWindowClick((PacketPlayInWindowClick) packet);
        else if(packet instanceof PacketPlayInSetCreativeSlot)
            return new InSetCreativeSlot((PacketPlayInSetCreativeSlot) packet);
        else if(packet instanceof PacketPlayOutEntityDestroy)
            return new OutEntityDestroy((PacketPlayOutEntityDestroy) packet);
        else if(packet instanceof PacketPlayOutEntityStatus)
            return new OutEntityStatus((PacketPlayOutEntityStatus) packet);
        else if(packet instanceof PacketPlayOutBlockBreakAnimation)
            return new OutBreakAnimation((PacketPlayOutBlockBreakAnimation) packet);
        else if(packet instanceof PacketPlayOutSpawnEntity)
            return new OutSpawnEntity((PacketPlayOutSpawnEntity) packet);
        else if(packet instanceof PacketPlayOutEntityMetadata)
            return new OutEntityMetadata((PacketPlayOutEntityMetadata) packet);
        else if(packet instanceof PacketPlayOutMultiBlockChange)
            return new OutMultiBlockChange((PacketPlayOutMultiBlockChange) packet);
        else if(packet instanceof PacketPlayOutBlockChange)
            return new OutBlockChange((PacketPlayOutBlockChange) packet);
        else if(packet instanceof PacketPlayOutEntityEffect)
            return new OutEntityEffect((PacketPlayOutEntityEffect) packet);
        else if(packet instanceof PacketPlayOutRemoveEntityEffect)
            return new OutRemoveEntityEffect((PacketPlayOutRemoveEntityEffect) packet);
        return null;
    }

    @Override
    public NMSBlock getBlock(Material material) {
        switch (material){
            case DISPENSER:
                return new BlockDispenser((net.minecraft.world.level.block.BlockDispenser) Blocks.ay);
        }
        return new Block(CraftMagicNumbers.getBlock(material));
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
        if(player.getVehicle() != null)
            player.getVehicle().eject();

        WorldServer toWorld = ((CraftWorld) location.getWorld()).getHandle();

        WorldServer fromWorld = ((CraftWorld) player.getWorld()).getHandle();

        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

        if(toWorld == fromWorld)
            entityPlayer.b.teleport(location);
        else
            MinecraftServer.getServer().ac().respawn(entityPlayer, toWorld, true, location, true);
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
