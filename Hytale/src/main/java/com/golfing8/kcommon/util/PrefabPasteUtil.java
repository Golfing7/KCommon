package com.golfing8.kcommon.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.prefab.PrefabLoadException;
import com.hypixel.hytale.server.core.prefab.PrefabStore;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Pastes prefabs into worlds.
 */
public final class PrefabPasteUtil {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private PrefabPasteUtil() {
    }

    /**
     * Pastes a prefab into the specified world at the given paste point.
     * <p>
     * The prefab is centered on the X/Z axes and anchored at the bottom Y level.
     * Surrounding chunks are preloaded to ensure proper placement.
     * @param world          The world to paste the prefab into
     * @param prefabFileName The name of the prefab file to load
     * @param pastePoint     The world coordinates where the prefab should be pasted
     * @return completes with whether the paste succeeded, after it ran on the world thread
     */
    public static CompletableFuture<Boolean> pastePrefab(
            @NotNull World world,
            @NotNull String prefabFileName,
            @NotNull Vector3i pastePoint
    ) {

        try {
            // Load prefab from server store
            BlockSelection cached = getServerPrefab(prefabFileName);
            if (cached == null) {
                LOGGER.atSevere().log("Server Prefab not found: " + prefabFileName);
                return CompletableFuture.completedFuture(false);
            }
            // PrefabStore caches and shares the selection; never mutate the shared copy
            BlockSelection prefab = cached.cloneSelection();

            // Set the anchor for proper centering
            Vector3i anchor = computeAnchor(prefabFileName, prefab);
            prefab.setAnchor(anchor.x, anchor.y, anchor.z);
            LOGGER.atWarning().log(
                    "Set anchor for prefab %s to (%d, %d, %d)", prefabFileName, anchor.x, anchor.y, anchor.z);

            // Placement skips unloaded chunks, so wait for them, then place on the target world's thread
            return preloadChunks(world, pastePoint, 4).thenApplyAsync(ignored -> {
                // Place prefab (no undo, no mask)
                prefab.placeNoReturn(world, pastePoint, world.getEntityStore().getStore());
                LOGGER.atInfo().log(
                        "Server Prefab %s placed in world %s at (%d, %d, %d) successfully", prefabFileName,
                        world.getName(), pastePoint.x, pastePoint.y, pastePoint.z
                );
                return true;
            }, world).exceptionally(e -> {
                LOGGER.atSevere().withCause(e).log(
                        "Failed to place prefab %s in world: %s", prefabFileName, world.getName());
                return false;
            });
        } catch (Exception e) {
            LOGGER.atSevere().withCause(e).log(
                    "Failed to place prefab %s in world: %s", prefabFileName, world.getName());
            return CompletableFuture.completedFuture(false);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private static CompletableFuture<Void> preloadChunks(World world, Vector3i origin, int radiusChunks) {
        List<CompletableFuture<?>> loads = new ArrayList<>();
        int originChunkX = origin.x >> 5;
        int originChunkZ = origin.z >> 5;

        for (int dx = -radiusChunks; dx <= radiusChunks; dx++) {
            for (int dz = -radiusChunks; dz <= radiusChunks; dz++) {
                long chunkIndex = ChunkUtil.indexChunk(originChunkX + dx, originChunkZ + dz);
                loads.add(world.getChunkStore().getChunkReferenceAsync(chunkIndex));
            }
        }
        return CompletableFuture.allOf(loads.toArray(CompletableFuture[]::new));
    }

    private static @Nullable BlockSelection getServerPrefab(
            @NotNull String prefabName
    ) {
        try {
            if (prefabName.endsWith(".prefab.json")) {
                return PrefabStore.get().getServerPrefab(prefabName);
            } else {
                return PrefabStore.get().getServerPrefab(prefabName + ".prefab.json");
            }
        } catch (PrefabLoadException e) {
            LOGGER.atSevere().withCause(e).log("Error loading prefab: " + prefabName);
            return null;
        }
    }

    private static @NotNull Vector3i computeAnchor(@NotNull String prefabFileName, @NotNull BlockSelection prefab) {
        // Use built-in selection bounds if available (not always set)
        if (prefab.hasSelectionBounds()) {
            // Compute centered X/Z and bottom Y anchor
            Vector3i min = prefab.getSelectionMin();
            Vector3i max = prefab.getSelectionMax();

            int anchorX = (min.x() + max.x()) / 2;
            int anchorY = min.y(); // bottom
            int anchorZ = (min.z() + max.z()) / 2;
            LOGGER.atWarning().log(
                    "Prefab Selection %s bounds: min(%d, %d, %d), max(%d, %d, %d), block count: %d, selection volume: %d",
                    prefabFileName, min.x(), min.y(), min.z(), max.x(), max.y(), max.z(),
                    prefab.getBlockCount(), prefab.getSelectionVolume()
            );
            return new Vector3i(anchorX, anchorY, anchorZ);
        }

        // Use manual size calculation as fallback
        LOGGER.atWarning().log("Prefab %s requires manual size calculation, processing...", prefabFileName);
        Vector3i size = manualSizeCalculation(prefab);
        int anchorX = size.x / 2;
        int anchorY = 0; // bottom
        int anchorZ = size.z / 2;
        LOGGER.atWarning().log(
                "Manually calculated prefab %s size: (%d, %d, %d)", prefabFileName, size.x, size.y, size.z);
        return new Vector3i(anchorX, anchorY, anchorZ);
    }

    private static @NotNull Vector3i manualSizeCalculation(
            @NotNull BlockSelection prefab
    ) {
        @Nullable Integer[] minX = {null};
        @Nullable Integer[] minY = {null};
        @Nullable Integer[] minZ = {null};
        @Nullable Integer[] maxX = {null};
        @Nullable Integer[] maxY = {null};
        @Nullable Integer[] maxZ = {null};
        prefab.forEachBlock((x, y, z, _) -> {
            if (minX[0] == null || x < minX[0]) {
                minX[0] = x;
            }
            if (minY[0] == null || y < minY[0]) {
                minY[0] = y;
            }
            if (minZ[0] == null || z < minZ[0]) {
                minZ[0] = z;
            }
            if (maxX[0] == null || x > maxX[0]) {
                maxX[0] = x;
            }
            if (maxY[0] == null || y > maxY[0]) {
                maxY[0] = y;
            }
            if (maxZ[0] == null || z > maxZ[0]) {
                maxZ[0] = z;
            }
        });
        // Validate all values are non-null
        if (minX[0] == null || minY[0] == null || minZ[0] == null || maxX[0] == null || maxY[0] == null ||
                maxZ[0] == null) {
            throw new IllegalStateException("Prefab contains no blocks");
        }
        return new Vector3i(maxX[0] - minX[0] + 1, maxY[0] - minY[0] + 1, maxZ[0] - minZ[0] + 1);
    }
}
