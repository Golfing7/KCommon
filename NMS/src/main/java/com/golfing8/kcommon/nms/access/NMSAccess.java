package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.world.NMSWorld;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface NMSAccess {
    NMSServer getMinecraftServer();

    NMSWorld getWorld(World world);

    NMSMagicNumberAccess getMagicNumbers();

    NMSPacket wrapPacket(Object packet);

    NMSBlock getBlock(Material material);

    NMSMagicEntities getMagicEntities();

    NMSMagicPackets getMagicPackets();

    NMSMagicItems getMagicItems();

    NMSMagicEvents getMagicEvents();

    WorldguardHook getWGHook();

    /**
     * Gets the world edit hook.
     *
     * @return the world edit hook.
     */
    WorldEditHook getWorldEditHook();

    void teleportPlayerNoEvent(Player player, Location location);

    void sendActionBar(Player player, String string);

    void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime);

    long getCurrentTick();
}
