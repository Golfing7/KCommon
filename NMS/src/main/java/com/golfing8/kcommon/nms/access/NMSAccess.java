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
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

/**
 * The main NMS accessor class.
 * <p>
 * Contains the structure for NMS access across the plugin.
 * </p>
 */
public interface NMSAccess {
    String ITEM_CAPTURE_NAME = "_";
    UUID ITEM_CAPTURE_UUID = UUID.randomUUID();

    /**
     * Gets the NMS server instance
     *
     * @return the nms server
     */
    NMSServer getMinecraftServer();

    /**
     * Gets the NMS wrapped world
     *
     * @param world the world
     * @return the nms world
     */
    NMSWorld getWorld(World world);

    /**
     * Gets the NMS craft magic numbers access
     *
     * @return the magic numbers access
     */
    NMSMagicNumberAccess getMagicNumbers();

    /**
     * Wraps the packet
     *
     * @param packet the packet
     * @return the wrapped packet
     */
    NMSPacket wrapPacket(Object packet);

    /**
     * Wraps the material
     *
     * @param material the material
     * @return the block
     */
    NMSBlock getBlock(Material material);

    /**
     * Gets NMS block access
     *
     * @return block access
     */
    NMSMagicBlocks getMagicBlocks();

    /**
     * Gets NMS entity access
     *
     * @return entity access
     */
    NMSMagicEntities getMagicEntities();

    /**
     * Gets NMS packet access
     *
     * @return packet access
     */
    NMSMagicPackets getMagicPackets();

    /**
     * Gets NMS item access
     *
     * @return item access
     */
    NMSMagicItems getMagicItems();

    /**
     * Gets NMS event access
     *
     * @return event access
     */
    NMSMagicEvents getMagicEvents();

    /**
     * Gets NMS inventory access
     *
     * @return inventory access
     */
    NMSMagicInventories getMagicInventories();

    /**
     * Gets the worldguard hook access
     *
     * @return worldguard hook
     */
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
    default void flushCommandQueue() {

    }

    /**
     * Removes the item capture player.
     */
    void removeItemCapturePlayer(ItemCapturePlayer player);

    /**
     * Gets the offline player if they are cached on the server
     *
     * @param str the string
     * @return the player
     */
    OfflinePlayer getOfflinePlayerIfCached(String str);

    /**
     * Teleport the player to the location without firing {@link PlayerTeleportEvent}
     *
     * @param player the player
     * @param location the location
     */
    void teleportPlayerNoEvent(Player player, Location location);

    /**
     * Sends the given action bar to the player
     *
     * @param player the player
     * @param string the action bar
     */
    void sendActionBar(Player player, String string);

    /**
     * Sends the title to the player
     *
     * @param player the player
     * @param title the title
     * @param subtitle the subtitle
     * @param fadeInTime the time in ticks fade in
     * @param showTime the time in ticks to show at full opacity
     * @param fadeOutTime the time in ticks fade out
     */
    void sendTitle(Player player, String title, String subtitle, int fadeInTime, int showTime, int fadeOutTime);

    /**
     * Gets the current server tick
     *
     * @return server tick
     */
    long getCurrentTick();
}
