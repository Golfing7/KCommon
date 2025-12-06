package com.golfing8.kcommon.nms.access;

import com.golfing8.kcommon.nms.ItemCapturePlayer;
import com.golfing8.kcommon.nms.block.NMSBlock;
import com.golfing8.kcommon.nms.packets.NMSPacket;
import com.golfing8.kcommon.nms.server.NMSServer;
import com.golfing8.kcommon.nms.world.NMSWorld;
import com.golfing8.kcommon.nms.worldedit.WorldEditHook;
import com.golfing8.kcommon.nms.worldguard.WorldguardHook;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public interface NMSAccess {
    String ITEM_CAPTURE_NAME = "_";
    UUID ITEM_CAPTURE_UUID = UUID.randomUUID();

    NMSServer getMinecraftServer();

    NMSWorld getWorld(World world);

    NMSMagicNumberAccess getMagicNumbers();

    NMSPacket wrapPacket(Object packet);

    NMSBlock getBlock(Material material);

    NMSMagicBlocks getMagicBlocks();

    NMSMagicEntities getMagicEntities();

    NMSMagicPackets getMagicPackets();

    NMSMagicItems getMagicItems();

    NMSMagicEvents getMagicEvents();

    NMSMagicInventories getMagicInventories();

    WorldguardHook getWGHook();

    /**
     * Gets the world edit hook.
     *
     * @return the world edit hook.
     */
    WorldEditHook getWorldEditHook();

    /**
     * Sends a minimessage formatted message to the player.
     *
     * @param sender the sender.
     * @param string the formatted message.
     */
    void sendMiniMessage(CommandSender sender, String string);

    /**
     * Broadcasts a component.
     *
     * @param component the component to broadcast
     */
    void broadcastComponent(Component component);

    /**
     * Creates an inventory and attempts to use mini message for the title.
     *
     * @param holder the holder of the inventory.
     * @param size   the size of the inventory.
     * @param title  the title of the inventory.
     * @return the inventory.
     */
    Inventory createInventory(InventoryHolder holder, int size, String title);

    /**
     * Creates an inventory and attempts to use mini message for the title.
     *
     * @param holder the holder of the inventory.
     * @param type   the type of the inventory.
     * @param title  the title of the inventory.
     * @return the inventory.
     */
    Inventory createInventory(InventoryHolder holder, InventoryType type, String title);

    /**
     * Checks if the server implementation currently supports persistent data containers.
     *
     * @return true if the server supports persistent data containers.
     */
    boolean supportsPersistentDataContainers();

    /**
     * Creates an item capture player and returns it.
     *
     * @return the craft player.
     */
    ItemCapturePlayer createPlayerForItemCapture();

    /**
     * Forces the current command queue to be flushed.
     */
    default void flushCommandQueue() {}

    /**
     * Removes the item capture player.
     */
    void removeItemCapturePlayer(ItemCapturePlayer player);

    OfflinePlayer getOfflinePlayerIfCached(String str);

    void teleportPlayerNoEvent(Player player, Location location);

    void sendActionBar(Player player, String string);

    void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime);

    long getCurrentTick();
}
