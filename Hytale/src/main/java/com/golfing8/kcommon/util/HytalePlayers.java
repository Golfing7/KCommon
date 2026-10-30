package com.golfing8.kcommon.util;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ActiveSlotInventoryComponent;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.component.PersistentDisplayName;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.item.ItemComponent;
import org.jetbrains.annotations.Nullable;
import java.util.*;
import java.util.function.Supplier;

/**
 * Helpers for working with Hytale players, whose components may only be touched on their world's thread.
 */
public final class HytalePlayers {
    private HytalePlayers() {
    }

    /**
     * Runs the action with the player's entity on their world's thread, right away if already on it.
     *
     * @param player the player.
     * @param action the action.
     * @return a future of true once the action ran, or false if the player had no entity in a world.
     */
    public static CompletableFuture<Boolean> runOnWorld(PlayerRef player, Consumer<Ref<EntityStore>> action) {
        UUID worldId = player.getWorldUuid();
        World world = worldId == null ? null : Universe.get().getWorld(worldId);
        if (world == null)
            return CompletableFuture.completedFuture(false);

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                // The reference is null or invalid while the player moves between worlds.
                Ref<EntityStore> ref = player.getReference();
                if (ref == null || !ref.isValid()) {
                    future.complete(false);
                    return;
                }
                action.accept(ref);
                future.complete(true);
            } catch (Throwable thr) {
                future.completeExceptionally(thr);
            }
        };
        if (world.isInThread())
            task.run();
        else
            world.execute(task);
        return future;
    }

    /**
     * Gets a future of a valid player object
     *
     * @param playerRef the player
     * @return the future
     */
    public static CompletableFuture<@Nullable Player> getPlayer(PlayerRef playerRef) {
        if (!playerRef.isValid() || playerRef.getWorldUuid() == null)
            return CompletableFuture.completedFuture(null);

        Ref<EntityStore> reference = playerRef.getReference();
        if (reference == null)
            return CompletableFuture.completedFuture(null);

        World world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null)
            return CompletableFuture.completedFuture(null);

        if (world.isInThread()) {
            return CompletableFuture.completedFuture(reference.getStore().getComponent(reference, Player.getComponentType()));
        } else {
            return CompletableFutureUtil._catch(CompletableFuture.supplyAsync(() -> {
                if (!reference.isValid())
                    return null;

                return reference.getStore().getComponent(reference, Player.getComponentType());
            }, world));
        }
    }

    /**
     * Gets the PlayerRef object from the player
     *
     * @param player the player
     * @return the ref
     */
    public static PlayerRef ref(Player player) {
        return player.getReference().getStore().getComponent(player.getReference(), PlayerRef.getComponentType());
    }

    /**
     * Gets the display name of the given player
     *
     * @param player the player
     * @return the display name
     */
    public static Message displayName(Player player) {
        DisplayNameComponent displayNameComponent = player.getReference().getStore().getComponent(player.getReference(), DisplayNameComponent.getComponentType());
        if (displayNameComponent == null) {
            PersistentDisplayName persistentDisplayName = player.getReference().getStore().getComponent(player.getReference(), PersistentDisplayName.getComponentType());
            if (persistentDisplayName == null) {
                return Message.raw(ref(player).getUsername());
            } else {
                return persistentDisplayName.getDisplayName();
            }
        } else {
            return displayNameComponent.getDisplayName();
        }
    }

    /**
     * Gets the component for the given player's UUID, supports offline players
     *
     * @param uuid the uuid
     * @return a future of the component
     * @param <T> the type
     */
    public static <T extends Component<EntityStore>> CompletableFuture<@Nullable T> getComponent(ComponentType<EntityStore, T> type, UUID uuid) {
        PlayerRef playerRef = Universe.get().getPlayer(uuid);
        if (playerRef == null) {
            return Universe.get().getPlayerStorage().load(uuid).thenApply(holder -> {
                if (holder == null)
                    return null;

                // Decoding the holder constructs a Player whose WorldMapTracker registers global event listeners.
                // Only Player#remove unregisters them, which never happens for an offline holder.
                Player player = holder.getComponent(Player.getComponentType());
                if (player != null)
                    player.getWorldMapTracker().unregisterEvents();
                return holder.getComponent(type);
            });
        } else if (playerRef.getHolder() != null) {
            return CompletableFuture.completedFuture(playerRef.getHolder().getComponent(type));
        } else if (playerRef.getWorldUuid() != null) {
            World world = Universe.get().getWorld(playerRef.getWorldUuid());
            if (world == null) {
                return CompletableFuture.completedFuture(null);
            }

            // The reference is null/invalid while the player transfers between worlds
            Supplier<T> read = () -> {
                Ref<EntityStore> reference = playerRef.getReference();
                return reference == null || !reference.isValid() ? null : reference.getStore().getComponent(reference, type);
            };
            if (world.isInThread()) {
                return CompletableFuture.completedFuture(read.get());
            } else {
                return CompletableFutureUtil._catch(CompletableFuture.supplyAsync(read, world));
            }
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * Gives the player the given item or drops it at their feet.
     *
     * @param player    the player to give the item to.
     * @param itemStack the item to give them.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack) {
        givePlayerItemsSafe(player, Collections.singleton(itemStack), false);
    }

    /**
     * Gives the player the given item or drops it at their feet.
     *
     * @param player    the player to give the item to.
     * @param itemStack the item to give them.
     * @param silent    if a message should be sent or not.
     */
    public static void givePlayerItemSafe(Player player, ItemStack itemStack, boolean silent) {
        givePlayerItemsSafe(player, Collections.singleton(itemStack), silent);
    }

    /**
     * Gives the player the given items or drops them at their feet.
     *
     * @param player     the player to give the item to.
     * @param itemStacks the items to give them.
     */
    public static void givePlayerItemsSafe(Player player, Collection<ItemStack> itemStacks) {
        givePlayerItemsSafe(player, itemStacks, false);
    }

    /**
     * Gives the player the given items safely, dropping excess on the ground
     *
     * @param player     the player
     * @param itemStacks the item stacks
     * @param silent     if the overflow message should be sent
     */
    public static void givePlayerItemsSafe(Player player, Collection<ItemStack> itemStacks, boolean silent) {
        CombinedItemContainer container = InventoryComponent.getCombined(
                player.getReference().getStore(),
                player.getReference(),
                InventoryComponent.HOTBAR_FIRST
        );
        TransformComponent transformComponent = player.getReference().getStore().getComponent(player.getReference(), TransformComponent.getComponentType());
        if (transformComponent == null)
            return;

        ListTransaction<ItemStackTransaction> transaction = container.addItemStacks(itemStacks.stream().toList());
        List<ItemStack> remainders = new ArrayList<>();
        for (ItemStackTransaction itemStackTransaction : transaction.getList()) {
            if (itemStackTransaction.getRemainder() == null)
                continue;

            remainders.add(itemStackTransaction.getRemainder());
        }

        if (remainders.isEmpty())
            return;

        Holder<EntityStore>[] items = ItemComponent.generateItemDrops(
                player.getWorld().getEntityStore().getStore(),
                remainders,
                transformComponent.getPosition(),
                transformComponent.getRotation()
        );
        player.getWorld().getEntityStore().getStore().addEntities(items, AddReason.SPAWN);
        if (!silent) {
            ref(player).sendMessage(HytaleMessages.toMessage("&cYour inventory was full. Check your feet!"));
        }
    }

    /**
     * Gets the player's full inventory
     *
     * @param player the player
     * @return the combined full inventory of the player
     */
    public static CombinedItemContainer getPlayerFullInventory(Player player) {
        return InventoryComponent.getCombined(
                player.getReference().getStore(),
                player.getReference(),
                InventoryComponent.EVERYTHING
        );
    }

    /**
     * Gets the active hotbar slot of the given player
     *
     * @param player the player
     * @return the hotbar slot
     */
    public static byte getActiveHotbarSlot(Player player) {
        return player.getReference().getStore().getComponent(
                player.getReference(),
                InventoryComponent.Hotbar.getComponentType()
        ).getActiveSlot();
    }

    /**
     * Gets the item in the hand of the given player
     *
     * @param player the player
     * @return the item
     */
    public static @Nullable ItemStack getItemInHand(Player player) {
        return InventoryComponent.getItemInHand(player.getReference().getStore(), player.getReference());
    }

    /**
     * Sets the item in the player's hand
     *
     * @param player the player
     * @param itemStack the itemstack
     */
    public static void setItemInHand(Player player, @Nullable ItemStack itemStack) {
        var store = player.getReference().getStore();
        // Mirror InventoryComponent#getItemInHand: the tools slot wins while it is in use
        InventoryComponent.Tool tool = store.getComponent(player.getReference(), InventoryComponent.Tool.getComponentType());
        ActiveSlotInventoryComponent hand = tool != null && tool.isUsingToolsItem()
                ? tool
                : store.getComponent(player.getReference(), InventoryComponent.Hotbar.getComponentType());
        hand.getInventory().setItemStackForSlot(hand.getActiveSlot(), itemStack);
    }
}
