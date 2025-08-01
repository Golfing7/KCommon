package com.golfing8.kcommon.data.local;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerAbstract;
import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.key.FieldIndexer;
import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.google.common.collect.Maps;
import com.google.gson.*;
import lombok.Getter;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public DataManagerLocal(String key, Plugin plugin, Class<T> typeClass) {
        super(key, plugin, typeClass);
        this.objectCache = new ConcurrentHashMap<>();
        this.directoryPrefix = Paths.get(getPlugin().getDataFolder().getPath(), "data", getKey());

        try {

            if (Files.notExists(this.directoryPrefix)) {
                // Copy over old file location.
                // TODO Remove this after a while.
                Path oldPath = Paths.get(KCommon.getInstance().getDataFolder().getPath(), "data", getKey());
                if (Files.exists(oldPath)) {
                    // Move it over.
                    FileUtils.moveDirectory(oldPath.toFile(), this.directoryPrefix.toFile());
                }
                Files.createDirectories(this.directoryPrefix);
            }
        } catch (IOException exc) {
            throw new RuntimeException(String.format("Failed to create directories for data manager with key %s!", key));
        }

        this.fieldIndexer = new FieldIndexerLocal<>(this);

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveAllChanged, 0, 600L);
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

        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            for (val entry : objectMap.entrySet()) {
                try {
                    saveObject(entry.getKey(), entry.getValue());
                } catch (IOException e) {
                    getPlugin().getLogger().severe(String.format("Failed to save object with key %s in data cache %s!", entry.getKey(), getKey()));
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
    public synchronized List<T> getWhere(String field, Object value, Object... keyValues) {
        return fieldIndexer.getWhere(field, value, keyValues);
    }

    @Override
    public synchronized void shutdown() {
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

                if (Files.isDirectory(path))
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
        Reader reader = Files.newBufferedReader(objPath);

        //Read the object in
        JsonElement object = JsonParser.parseReader(reader);

        String key = objPath.getFileName().toString().replace(".json", "");
        //Create an empty instance
        T newObject;
        try {
            newObject = DataSerializer.getGSONBase().fromJson(object, getTypeClass());
        } catch (JsonParseException exc) {
            getPlugin().getLogger().log(Level.WARNING, String.format("Cache %s failed to load object %s!", getTypeClass().getName(), key), exc);
            newObject = null;
        }
        if (newObject == null) {
            Files.delete(objPath);
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
        Path objPath = directoryPrefix.resolve(String.format("%s.json", obj.getKey()));
        if (Files.notExists(objPath))
            Files.createFile(objPath);

        try (Writer writer = Files.newBufferedWriter(objPath)) {
            writer.write(DataSerializer.getGSONBase().toJson(obj));
            writer.flush();
        }
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

        Path objPath = directoryPrefix.resolve(String.format("%s.json", key));
        if (Files.notExists(objPath))
            Files.createFile(objPath);

        try (Writer writer = Files.newBufferedWriter(objPath)) {
            writer.write(DataSerializer.getGSONBase().toJson(object));
            writer.flush();
        }
    }
}
