package com.golfing8.kcommon.data.remote;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerAbstract;
import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.key.FieldIndexer;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.golfing8.kcommon.struct.helper.promise.Promise;
import com.golfing8.kcommon.util.FoliaSchedulers;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReplaceOptions;
import lombok.Getter;
import lombok.val;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;

/**
 * Implements the {@link DataManager datamanager interface} on a remote level, storing the objects in MongoDB.
 */
public class DataManagerRemote<T extends DataSerializable> extends DataManagerAbstract<T> {
    /**
     * The document field the object's key is stored under.
     * <br>
     * This must match the field {@link com.golfing8.kcommon.data.AbstractSerializable} serializes its key with.
     */
    public static final String KEY_FIELD = "_objectId";
    /**
     * The prefix of the redis channel a manager announces its changes on.
     */
    private static final String CHANNEL_PREFIX = "kcommon:data:";
    /**
     * The prefix of the redis key a manager locks a single object with.
     */
    private static final String LOCK_PREFIX = "kcommon:lock:";
    /**
     * The signal sent after an object was written.
     */
    private static final String SIGNAL_UPDATE = "UPDATE";
    /**
     * The signal sent after an object was deleted.
     */
    private static final String SIGNAL_DELETE = "DELETE";

    /**
     * Caches the objects in a map for faster loading.
     */
    private final Map<String, T> objectCache;
    /**
     * The backing mongo collection. Exposed for atomic operations this manager doesn't provide.
     */
    @Getter
    private final MongoCollection<Document> collection;
    /**
     * Signals other instances about the objects in this manager.
     * <br>
     * This manager's cache only covers this JVM. When several servers share a database they each need to be told
     * that an object changed, otherwise they keep handing out a copy that is no longer what the database holds.
     * Without redis this is a no-op adapter and the manager behaves exactly as it did before.
     */
    @Getter
    private final RedisAdapter redisAdapter;
    /**
     * The redis channel this manager announces its changes on. Derived from the collection, so every instance of
     * the same manager listens to the same channel.
     */
    @Getter
    private final String channel;
    /**
     * The prefix of the lock key of an object in this manager.
     */
    private final String lockPrefix;
    /**
     * Held so the listener can be pulled back off the adapter when this manager shuts down.
     */
    private final Consumer<String> signalListener = this::acceptSignal;

    public DataManagerRemote(String key, Plugin plugin, Class<T> typeClass) {
        super(key, plugin, typeClass);
        this.objectCache = new ConcurrentHashMap<>();

        FoliaSchedulers.of(plugin).runTimer(this::saveAllChanged, 0, 600L);
        if (KCommon.getInstance().getConnector() == null)
            throw new IllegalStateException("Cannot connect to MongoDatabase");

        String collectionName = plugin.getName() + "_" + key;
        collection = KCommon.getInstance().getConnector().getDatabase().getCollection(collectionName);
        try {
            collection.createIndex(Indexes.ascending(KEY_FIELD), new IndexOptions().unique(true));
        } catch (Exception exc) {
            plugin.getLogger().log(Level.WARNING, String.format("Failed to create the unique key index on data cache %s.", key), exc);
        }

        this.redisAdapter = KCommon.getInstance().getRedisAdapter();
        this.channel = CHANNEL_PREFIX + collectionName;
        this.lockPrefix = LOCK_PREFIX + collectionName + ":";
        if (this.redisAdapter.isConnected())
            this.redisAdapter.subscribe(this.channel, this.signalListener);
    }

    /**
     * Handles a signal another instance sent about an object in this manager.
     * <br>
     * Runs on the adapter's subscriber thread, so it only touches the cache map and never the collection.
     *
     * @param message the raw message
     */
    private void acceptSignal(String message) {
        String[] split = message.split("\\|", 3);
        if (split.length != 3 || split[0].equals(this.redisAdapter.getInstanceId()))
            return;

        T cached = this.objectCache.get(split[2]);
        if (cached == null)
            return;

        // Dropping an object with unsaved changes would throw them away, and the other instance has already
        // written over them anyway. Both sides changed the same object, which is what locking is for.
        if (cached.hasChanged()) {
            getPlugin().getLogger().warning(String.format("Object %s of data cache %s changed on another instance while it had unsaved changes here.", split[2], getKey()));
            return;
        }

        this.objectCache.remove(split[2], cached);
    }

    /**
     * Tells every other instance that the object with the given key changed and that their copy is stale.
     * <br>
     * The manager does this for itself whenever it writes or deletes an object. Call it after writing through
     * the backing collection directly.
     *
     * @param key the key of the object
     */
    public void signalChange(@NotNull String key) {
        signal(SIGNAL_UPDATE, key);
    }

    /**
     * Publishes a signal about the object with the given key.
     *
     * @param action the action taken on the object
     * @param key    the key of the object
     */
    private void signal(String action, String key) {
        if (!this.redisAdapter.isConnected())
            return;

        this.redisAdapter.publishAsync(this.channel, this.redisAdapter.getInstanceId() + "|" + action + "|" + key);
    }

    /**
     * Runs the given supplier while this instance holds the network wide lock on the object with the given key.
     * <br>
     * Use this around a read followed by a write, which is the shape that loses data when two servers do it at
     * once. Without redis there is nobody to lock against and the supplier simply runs.
     *
     * @param key      the key of the object to lock
     * @param supplier the supplier to run
     * @param <R>      the type returned by the supplier
     * @return whatever the supplier returned
     * @throws IllegalStateException if the lock is held by another instance for longer than the wait time
     */
    public <R> R withLock(@NotNull String key, @NotNull Supplier<R> supplier) {
        String lockKey = this.lockPrefix + key;
        String token = this.redisAdapter.lock(lockKey, RedisAdapter.DEFAULT_LOCK_TIMEOUT, RedisAdapter.DEFAULT_LOCK_WAIT);
        if (token == null)
            throw new IllegalStateException(String.format("Failed to lock object %s of data cache %s.", key, getKey()));

        try {
            return supplier.get();
        } finally {
            this.redisAdapter.unlock(lockKey, token);
        }
    }

    /**
     * Runs the given task while this instance holds the network wide lock on the object with the given key.
     *
     * @param key      the key of the object to lock
     * @param runnable the task to run
     * @throws IllegalStateException if the lock is held by another instance for longer than the wait time
     */
    public void withLock(@NotNull String key, @NotNull Runnable runnable) {
        withLock(key, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * Saves all objects that have been 'changed'.
     */
    private void saveAllChanged() {
        Map<String, JsonObject> objectMap = new HashMap<>();
        Gson base = DataSerializer.getGSONBase();
        for (T obj : objectCache.values()) {
            if (obj.hasChanged()) {
                // Marked before serializing so a change made during the write isn't lost.
                obj.markSaved();
                objectMap.put(obj.getKey(), base.toJsonTree(obj).getAsJsonObject());
            }
        }

        if (objectMap.isEmpty())
            return;

        FoliaSchedulers.of(getPlugin()).runAsync(() -> {
            for (val entry : objectMap.entrySet()) {
                try {
                    saveObject(entry.getKey(), entry.getValue());
                    signal(SIGNAL_UPDATE, entry.getKey());
                } catch (Exception e) {
                    // Restore the changed flag so the object is retried on the next cycle.
                    T cached = objectCache.get(entry.getKey());
                    if (cached != null)
                        cached.change();

                    getPlugin().getLogger().log(Level.SEVERE, String.format("Failed to save object with key %s in data cache %s!", entry.getKey(), getKey()), e);
                }
            }
        });
    }

    @Override
    public FieldIndexer<T> getAlternateKeyingManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public synchronized T getOrCreate(@NotNull String key) {
        T obj = getObject(key);
        if (obj != null)
            return obj;

        if (!this.redisAdapter.isConnected())
            return createAndStore(key);

        // Another instance may be creating the same object right now. Claiming it first means the loser of that
        // race reads what the winner wrote instead of overwriting it with an empty object.
        String lockKey = this.lockPrefix + key;
        String token = this.redisAdapter.lock(lockKey, RedisAdapter.DEFAULT_LOCK_TIMEOUT, RedisAdapter.DEFAULT_LOCK_WAIT);
        if (token == null) {
            getPlugin().getLogger().warning(String.format("Timed out locking object %s of data cache %s, creating it anyway.", key, getKey()));
            return createAndStore(key);
        }

        try {
            T fresh = loadFresh(key);
            return fresh != null ? fresh : createAndStore(key);
        } finally {
            this.redisAdapter.unlock(lockKey, token);
        }
    }

    /**
     * Creates an empty object with the given key and writes it.
     *
     * @param key the key of the object
     * @return the created object
     */
    private T createAndStore(String key) {
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

        T loaded;
        try {
            loaded = this.loadObject(key);
        } catch (Exception exc) {
            throw new RuntimeException(String.format("Failed load object with type %s with key %s!", getTypeClass().getName(), key), exc);
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

    /**
     * Loads the object with the given key straight from the database, ignoring (and replacing) the cached copy.
     * <br>
     * The cache of this manager is local to this server. Any decision that depends on the object being up to date
     * across a network must read through this method, ideally while holding a lock on the object.
     *
     * @param key the key
     * @return the object, or null if it doesn't exist
     */
    @Nullable
    public synchronized T loadFresh(@NotNull String key) {
        T loaded;
        try {
            loaded = this.loadObject(key);
        } catch (Exception exc) {
            throw new RuntimeException(String.format("Failed load object with type %s with key %s!", getTypeClass().getName(), key), exc);
        }

        if (loaded == null) {
            this.objectCache.remove(key);
            return null;
        }

        this.objectCache.put(key, loaded);
        return loaded;
    }

    /**
     * Loads the object with the given key straight from the database asynchronously.
     *
     * @param key the key
     * @return the promise of the object
     */
    public Promise<@Nullable T> loadFreshAsync(@NotNull String key) {
        return Promise.supplyingAsync(() -> loadFresh(key));
    }

    @Override
    public synchronized List<T> getWhere(String field, Object value, Object... keyValues) {
        List<T> toReturn = new ArrayList<>();
        for (Document document : collection.find(buildFilter(field, value, keyValues))) {
            T loaded = deserialize(document);
            if (loaded == null)
                continue;

            this.objectCache.put(loaded.getKey(), loaded);
            toReturn.add(loaded);
        }
        return toReturn;
    }

    @Override
    public Promise<List<T>> getWhereAsync(String field, Object value, Object... keyValues) {
        return Promise.supplyingAsync(() -> getWhere(field, value, keyValues));
    }

    /**
     * Builds a filter matching every provided field/value pair.
     *
     * @param field     the first field
     * @param value     the value of the first field
     * @param keyValues more field/value pairs
     * @return the filter
     */
    private Bson buildFilter(String field, Object value, Object... keyValues) {
        if (keyValues.length % 2 != 0)
            throw new IllegalArgumentException("Field/value pairs must be even, got " + keyValues.length);

        List<Bson> filters = new ArrayList<>();
        filters.add(Filters.eq(field, value));
        for (int i = 0; i < keyValues.length; i += 2) {
            filters.add(Filters.eq(String.valueOf(keyValues[i]), keyValues[i + 1]));
        }
        return filters.size() == 1 ? filters.get(0) : Filters.and(filters);
    }

    @Override
    public synchronized void shutdown() {
        this.redisAdapter.unsubscribe(this.channel, this.signalListener);
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

        //Load all from the collection
        for (Document document : collection.find()) {
            String id = document.getString(KEY_FIELD);
            if (id == null || objectCache.containsKey(id))
                continue;

            T loaded = deserialize(document);
            if (loaded == null)
                continue;

            toReturn.put(loaded.getKey(), loaded);
            objectCache.put(loaded.getKey(), loaded);
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
        this.objectCache.remove(key);
        boolean deleted = this.collection.findOneAndDelete(Filters.eq(KEY_FIELD, key)) != null;
        signal(SIGNAL_DELETE, key);
        return deleted;
    }

    @Override
    public synchronized boolean store(@NotNull T obj) {
        boolean replacing = this.objectExists(obj.getKey());
        this.writeObject(obj);
        this.objectCache.put(obj.getKey(), obj);
        obj.markSaved();
        signal(SIGNAL_UPDATE, obj.getKey());
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
     * Checks the collection for an object with the given key
     *
     * @param key the key of the object being stored
     * @return true if it exists, false if not
     */
    private boolean objectExists(String key) {
        return collection.find(Filters.eq(KEY_FIELD, key)).first() != null;
    }

    /**
     * Loads the given object with the given key
     *
     * @param objKey the path key of the object
     * @return the object loaded
     */
    private T loadObject(String objKey) {
        Document document = collection.find(Filters.eq(KEY_FIELD, objKey)).first();
        if (document == null)
            return null;

        return deserialize(document);
    }

    /**
     * Converts a raw document from this manager's collection into an object.
     * <br>
     * Useful when a caller runs its own query through the backing collection and wants the objects without
     * a second round trip per document. The returned object is not cached.
     *
     * @param document the document
     * @return the object, or null if it couldn't be parsed
     */
    @Nullable
    public T fromDocument(@NotNull Document document) {
        return deserialize(document);
    }

    /**
     * Deserializes the given document into an object of this manager's type.
     *
     * @param document the document
     * @return the object, or null if it couldn't be parsed
     */
    @Nullable
    private T deserialize(Document document) {
        String objKey = document.getString(KEY_FIELD);
        T newObject;
        try {
            newObject = DataSerializer.getGSONBase().fromJson(document.toJson(), getTypeClass());
        } catch (JsonParseException exc) {
            getPlugin().getLogger().log(Level.WARNING, String.format("Cache %s failed to load object %s!", getTypeClass().getName(), objKey), exc);
            return null;
        }
        if (newObject == null)
            return null;

        newObject.setKey(objKey);
        return newObject;
    }

    /**
     * Writes the given object to the collection
     *
     * @param obj the object to write
     */
    private void writeObject(T obj) {
        saveObject(obj.getKey(), DataSerializer.getGSONBase().toJsonTree(obj).getAsJsonObject());
    }

    /**
     * Saves an object to the database.
     *
     * @param key    the key of the object.
     * @param object the object.
     */
    private void saveObject(String key, JsonObject object) {
        if (object == null)
            return;

        Document objectDocument = Document.parse(object.toString());
        objectDocument.put(KEY_FIELD, key);
        collection.replaceOne(Filters.eq(KEY_FIELD, key), objectDocument, new ReplaceOptions().upsert(true));
    }
}
