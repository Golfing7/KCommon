package com.golfing8.kcommon.data.local;

import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerAbstract;
import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.key.FieldIndexer;
import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.golfing8.kcommon.platform.Platform;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Getter;
import lombok.val;
import com.golfing8.kcommon.KPluginContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Implements the {@link DataManager datamanager interface} on a local level, storing the objects in files.
 */
public class DataManagerLocal<T extends DataSerializable> extends DataManagerAbstract<T> {
    /**
     * The directory prefix of where we're storing/loading data from.
     */
    @Getter
    private final Path directoryPrefix;
    /**
     * Caches the objects in a map for faster loading.
     */
    private final Map<String, T> objectCache;
    /**
     * The manager for alternative keys
     */
    private final FieldIndexerLocal<T> fieldIndexer;
    /**
     * The autosave task, cancelled when this manager shuts down.
     */
    private final Terminable autosaveTask;

    public DataManagerLocal(String key, KPluginContext plugin, Class<T> typeClass) {
        super(key, plugin, typeClass);
        this.objectCache = new ConcurrentHashMap<>();
        this.directoryPrefix = Paths.get(getPlugin().getPluginDataFolder().toString(), "data", getKey());

        try {

            if (Files.notExists(this.directoryPrefix)) {
                // Copy over old file location.
                // TODO Remove this after a while.
                Path oldPath = Paths.get(Platform.get().getKCommon().getPluginDataFolder().toString(), "data", getKey());
                if (Files.exists(oldPath)) {
                    // Move it over.
                    Files.createDirectories(this.directoryPrefix.getParent());
                    Files.move(oldPath, this.directoryPrefix);
                }
                Files.createDirectories(this.directoryPrefix);
            }
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to create directories for data manager with key %s!", key));
        }

        this.fieldIndexer = new FieldIndexerLocal<>(this);

        this.autosaveTask = Platform.get().runTimer(this::saveAllChanged, 0, 600L);
    }

    /**
     * Saves all objects that have been 'changed'.
     */
    private void saveAllChanged() {
        Map<String, JsonObject> objectMap = new HashMap<>();
        Gson base = DataSerializer.getGSONBase();
        for (T obj : objectCache.values()) {
            if (obj.hasChanged()) {
                obj.markSaved();
                objectMap.put(obj.getKey(), base.toJsonTree(obj).getAsJsonObject());
            }
        }

        Platform.get().runAsync(() -> {
            for (val entry : objectMap.entrySet()) {
                try {
                    saveObject(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    getPlugin().getPluginLogger().severe(String.format("Failed to save object with key %s in data cache %s!", entry.getKey(), getKey()));
                }
            }
        });
    }

    @Override
    public FieldIndexer<T> getAlternateKeyingManager() {
        return fieldIndexer;
    }

    @Override
    public synchronized T getOrCreate(@NotNull String key) {
        T obj = getObject(key);
        if (obj != null)
            return obj;

        // Otherwise, we must create one.
        T created = createEmpty();
        created.setKey(key);
        store(created);
        return created;
    }

    @Override
    public Promise<T> getOrCreateAsync(@NotNull String key) {
        return Promise.supplyingAsync(() -> getOrCreate(key));
    }

    @Nullable
    @Override
    public synchronized T getObject(@NotNull String key) {
        if (this.objectCache.containsKey(key))
            return this.objectCache.get(key);

        //Check if the object exists
        if (!this.objectExists(key)) {
            return null;
        }

        T loaded;
        try {
            loaded = this.loadObject(directoryPrefix.resolve(String.format("%s.json", key)));
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed load object with type %s with key %s!", getTypeClass().getName(), key));
        }
        if (loaded == null)
            return null;

        this.objectCache.put(key, loaded);
        return loaded;
    }

    @Override
    public Promise<T> getObjectAsync(@NotNull String key) {
        return Promise.supplyingAsync(() -> getObject(key));
    }

    @Override
    public synchronized List<T> getWhere(String field, Object value, Object... keyValues) {
        return fieldIndexer.getWhere(field, value, keyValues);
    }

    @Override
    public Promise<List<T>> getWhereAsync(String field, Object value, Object... keyValues) {
        return Promise.supplyingAsync(() -> getWhere(field, value, keyValues));
    }

    @Override
    public synchronized void shutdown() {
        // Otherwise every module reload leaves another timer saving this stale cache.
        this.autosaveTask.closeSilently();
        this.objectCache.forEach((key, value) -> {
            if (isStrictSaving() && !value.hasChanged())
                return;

            this.store(value);
        });
    }

    @Override
    public synchronized void uncache(@NotNull String key) {
        this.objectCache.remove(key);
    }

    @Override
    public synchronized Map<String, T> getAll() {
        Map<String, T> toReturn = Maps.newHashMap();

        //Load all from the files
        try (Stream<Path> dirStream = Files.walk(this.directoryPrefix)) {
            dirStream.forEach(path -> {
                // Check if the cache has already loaded that object.
                if (objectCache.containsKey(path.getFileName().toString().replace(".json", "")))
                    return;

                // Skips folders, unfinished writes and unreadable objects that were set aside.
                if (Files.isDirectory(path) || !path.getFileName().toString().endsWith(".json"))
                    return;

                try {
                    T t = this.loadObject(path);
                    if (t == null)
                        return;

                    toReturn.put(t.getKey(), t);
                    objectCache.put(t.getKey(), t);
                } catch (IOException e) {
                    throw new RuntimeException(String.format("Failed to load object with type %s under path %s!", getTypeClass().getName(), path), e);
                }
            });
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to open directory stream for manager with type %s!", getTypeClass().getName()), exc);
        }

        toReturn.putAll(this.objectCache);
        return toReturn;
    }

    @Override
    public Promise<Collection<T>> getAllAsync() {
        return Promise.supplyingAsync(() -> getAll().values());
    }

    @Override
    public Map<String, T> getAllCached() {
        return Collections.unmodifiableMap(this.objectCache);
    }

    @Override
    public synchronized boolean delete(@NotNull String key) {
        if (!this.objectExists(key))
            return false;

        this.objectCache.remove(key);
        try {
            Files.delete(directoryPrefix.resolve(String.format("%s.json", key)));
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to delete object with type %s with key %s!", this.getTypeClass().getName(), key));
        }
        return true;
    }

    @Override
    public synchronized boolean store(@NotNull T obj) {
        boolean replacing = this.exists(obj.getKey());
        try {
            this.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Failed to write object with type %s with key %s!", this.getTypeClass().getName(), obj.getKey()), e);
        }
        this.objectCache.put(obj.getKey(), obj);
        obj.markSaved();
        return !replacing;
    }

    @Override
    public synchronized void save(@NotNull String key) {
        if (!this.objectCache.containsKey(key))
            return;

        store(this.objectCache.get(key));
    }

    @Override
    public synchronized boolean exists(@NotNull String key) {
        return this.objectCache.containsKey(key) || this.objectExists(key);
    }

    /**
     * Checks the local file directory for a object with the given key
     *
     * @param key the key of the object being stored
     * @return true if it exists, false if not
     */
    private boolean objectExists(String key) {
        return Files.exists(directoryPrefix.resolve(String.format("%s.json", key)));
    }

    /**
     * Loads the given object with the given key
     *
     * @param objPath the path of the object to load
     * @return the object loaded
     * @throws IOException if there's a reading error
     */
    private T loadObject(Path objPath) throws IOException {
        String key = objPath.getFileName().toString().replace(".json", "");
        //Create an empty instance
        T newObject;
        try (Reader reader = Files.newBufferedReader(objPath)) {
            newObject = DataSerializer.getGSONBase().fromJson(JsonParser.parseReader(reader), getTypeClass());
        } catch (JsonParseException exc) {
            getPlugin().getPluginLogger().log(Level.WARNING, String.format("Cache %s failed to load object %s!", getTypeClass().getName(), key), exc);
            newObject = null;
        }
        if (newObject == null) {
            // Keep the unreadable file for recovery instead of deleting it; the next save writes a fresh one.
            Path corrupt = objPath.resolveSibling(objPath.getFileName() + ".corrupt-" + System.currentTimeMillis());
            Files.move(objPath, corrupt);
            getPlugin().getPluginLogger().severe(String.format("Moved unreadable object %s of cache %s to %s", key, getKey(), corrupt.getFileName()));
            return null;
        }
        newObject.setKey(key);
        return newObject;
    }

    /**
     * Writes the given object to a file
     *
     * @param obj the object to write
     * @throws IOException if there's a writing error
     */
    private void writeObject(T obj) throws IOException {
        writeAtomically(directoryPrefix.resolve(String.format("%s.json", obj.getKey())), DataSerializer.getGSONBase().toJson(obj));
    }

    /**
     * Saves an object to a file.
     *
     * @param key    the key of the object.
     * @param object the object.
     * @throws IOException if there's a writing error.
     */
    private void saveObject(String key, JsonObject object) throws IOException {
        if (object == null)
            return;

        writeAtomically(directoryPrefix.resolve(String.format("%s.json", key)), DataSerializer.getGSONBase().toJson(object));
    }

    /**
     * Writes a temporary file and moves it over the target, so a crash mid-write never leaves a half written object.
     */
    private static void writeAtomically(Path target, String json) throws IOException {
        Path temp = target.resolveSibling(target.getFileName() + ".tmp");
        try (Writer writer = Files.newBufferedWriter(temp)) {
            writer.write(json);
        }
        try {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exc) {
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
