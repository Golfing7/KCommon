package com.golfing8.kcommon.data.remote;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.data.DataManager;
import com.golfing8.kcommon.data.DataManagerAbstract;
import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.key.FieldIndexer;
import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lombok.val;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements the {@link DataManager datamanager interface} on a local level, storing the objects in files.
 */
public class DataManagerRemote<T extends DataSerializable> extends DataManagerAbstract<T> {
    /**
     * Caches the objects in a map for faster loading.
     */
    private final Map<String, T> objectCache;
    private final MongoCollection<Document> collection;

    public DataManagerRemote(String key, Plugin plugin, Class<T> typeClass) {
        super(key, plugin, typeClass);
        this.objectCache = new ConcurrentHashMap<>();

        plugin.getServer().getScheduler().runTaskTimer(plugin, this::saveAllChanged, 0, 600L);
        if (KCommon.getInstance().getConnector() == null)
            throw new IllegalStateException("Cannot connect to MongoDatabase");

        collection = KCommon.getInstance().getConnector().getDatabase().getCollection(plugin.getName() + "_" + key);
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
                } catch (Exception e) {
                    getPlugin().getLogger().severe(String.format("Failed to save object with key %s in data cache %s!", entry.getKey(), getKey()));
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

        // Otherwise, we must create one.
        T created = createEmpty();
        created.setKey(key);
        store(created);
        return created;
    }

    @Nullable
    @Override
    public synchronized T getObject(@Nonnull String key) {
        if (this.objectCache.containsKey(key))
            return this.objectCache.get(key);

        //Check if the object exists
        if (!this.objectExists(key)) {
            return null;
        }

        T loaded;
        try {
            loaded = this.loadObject(key);
        } catch (Exception exc) {
            throw new RuntimeException(String.format("Failed load object with type %s with key %s!", getTypeClass().getName(), key));
        }
        if (loaded == null)
            return null;

        this.objectCache.put(key, loaded);
        return loaded;
    }

    @Override
    public synchronized List<T> getWhere(String field, Object value, Object... keyValues) {
        throw new UnsupportedOperationException("Not supported yet.");
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
    public synchronized void uncache(@Nonnull String key) {
        this.objectCache.remove(key);
    }

    @Override
    public synchronized Map<String, T> getAll() {
        Map<String, T> toReturn = Maps.newHashMap();

        //Load all from the files
        for (Document document : collection.find()) {
            String id = document.getObjectId("_key").toString();
            if (objectCache.containsKey(id))
                continue;

            try {
                T t = this.loadObject(id);
                if (t == null)
                    continue;

                toReturn.put(t.getKey(), t);
                objectCache.put(t.getKey(), t);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Failed to load object with type %s with id %s!", getTypeClass().getName(), id), e);
            }
        }

        toReturn.putAll(this.objectCache);
        return toReturn;
    }

    @Override
    public Map<String, T> getAllCached() {
        return new HashMap<>(this.objectCache);
    }

    @Override
    public synchronized boolean delete(@Nonnull String key) {
        if (!this.objectExists(key))
            return false;

        this.objectCache.remove(key);
        this.collection.findOneAndDelete(Filters.eq(new ObjectId(key)));
        return true;
    }

    @Override
    public synchronized boolean store(@Nonnull T obj) {
        boolean replacing = this.exists(obj.getKey());
        this.writeObject(obj);
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
    public synchronized boolean exists(@Nonnull String key) {
        return this.objectCache.containsKey(key) || this.objectExists(key);
    }

    /**
     * Checks the local file directory for a object with the given key
     *
     * @param key the key of the object being stored
     * @return true if it exists, false if not
     */
    private boolean objectExists(String key) {
        return collection.find(Filters.eq("_key", key)).first() != null;
    }

    /**
     * Loads the given object with the given key
     *
     * @param objKey the path key of the object
     * @return the object loaded
     */
    private T loadObject(String objKey) {
        Bson filter = Filters.eq("_key", objKey);
        Document document = collection.find(filter).first();
        if (document == null)
            return null;

        // TODO Object conversion
        //Create an empty instance
        T newObject;
        try {
            newObject = DataSerializer.getGSONBase().fromJson(document.toJson(), getTypeClass());
        } catch (JsonParseException exc) {
            getPlugin().getLogger().warning(String.format("Cache %s failed to load object %s!", getTypeClass().getName(), objKey));
            if (KCommon.getInstance().isDebug()) {
                exc.printStackTrace();
            }
            newObject = null;
        }
        if (newObject == null) {
            collection.findOneAndDelete(filter);
            return null;
        }

        newObject.setKey(objKey);
        return newObject;
    }

    /**
     * Writes the given object to a file
     *
     * @param obj the object to write
     */
    private void writeObject(T obj) {
        Document objectDocument = Document.parse(DataSerializer.getGSONBase().toJson(obj)); // TODO Improve this.
        collection.replaceOne(Filters.eq("_key", obj.getKey()), objectDocument, new ReplaceOptions().upsert(true));
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

        Document objectDocument = Document.parse(DataSerializer.getGSONBase().toJson(object.toString())); // TODO Improve this.
        collection.replaceOne(Filters.eq("_key", key), objectDocument, new ReplaceOptions().upsert(true));
    }
}
