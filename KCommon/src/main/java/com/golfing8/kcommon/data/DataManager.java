package com.golfing8.kcommon.data;

import com.golfing8.kcommon.data.key.FieldIndexer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Represents an abstract data manager, used to manage data stored somewhere.
 */
public interface DataManager<T extends DataSerializable> {
    /**
     * Gets the key of this data manager.
     *
     * @return the key.
     */
    String getKey();

    /**
     * Generates a new unique key NOT USED in this data manager for storage.
     *
     * @return the new unique key.
     */
    UUID getUniqueKey();

    /**
     * Gets the alternate keying manager for this data manager.
     *
     * @return the alternate keying manager.
     */
    FieldIndexer<T> getAlternateKeyingManager();

    /**
     * Gets, or creates, a given object with the given key.
     *
     * @param key the key.
     * @return the object.
     */
    T getOrCreate(@Nonnull String key);

    /**
     * Gets the object from the data manager, or null if it doesn't exist.
     * <p />
     * If the object did exist, it is saved in the cache.
     *
     * @param key the key
     * @return the object
     */
    @Nullable T getObject(@Nonnull String key);

    /**
     * Gets all objects that have field values equal to the provided parameters.
     *
     * @param field the first field to get an object with.
     * @param value the value of the first field.
     * @param keyValues more field/value pairs.
     * @return all objects that have those values.
     */
    List<T> getWhere(String field, Object value, Object... keyValues);

    /**
     * Shuts this data manager down, saving all cached objects.
     */
    void shutdown();

    /**
     * Uncaches the object with given key from this data manager
     *
     * @param key the key of the object to uncache
     */
    void uncache(@Nonnull String key);

    /**
     * Gets all objects stored in the data manager
     *
     * @return a map of all objects mapped from their keys
     */
    Map<String, T> getAll();

    /**
     * Deletes an object from the data manager
     *
     * @param obj the object to delete
     * @return true if deleted, false if the object wasn't found
     */
    default boolean delete(@Nonnull T obj){return this.delete(obj.getKey());}

    /**
     * Deletes an object with the given key
     *
     * @param key the key of the object to delete
     * @return true if deleted, false if the object wasn't found
     */
    boolean delete(@Nonnull String key);

    /**
     * Stores the object in this data manager. This will delete any object that already exists with the same key
     *
     * @param obj the object to store
     * @return true if the object didn't previously exist, false if we're replacing something
     */
    boolean store(@Nonnull T obj);

    /**
     * Checks if an object is stored under the given key in the data manager
     *
     * @param key the key of the object being stored
     * @return true if it does, false if not
     */
    boolean exists(@Nonnull String key);
}
