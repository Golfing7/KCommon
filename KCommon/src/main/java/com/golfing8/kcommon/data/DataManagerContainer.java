package com.golfing8.kcommon.data;

import com.golfing8.kcommon.data.local.DataManagerLocal;
import com.golfing8.kcommon.data.remote.DataManagerRemote;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Represents a container of data managers.
 * <p>
 * Classes that implement this act as Class to DataManager maps.
 * </p>
 */
public interface DataManagerContainer {
    Map<Class<? extends DataManagerContainer>, Map<Class<?>, DataManager<?>>> c2DataManagers = new HashMap<>();
    Map<Class<? extends DataManagerContainer>, Map<String, DataManager<?>>> dataManagers = new HashMap<>();

    /**
     * Gets the data manager map backing this container.
     *
     * @return the map.
     * @param <T> the type of data serializable.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    default <T extends DataSerializable> Map<Class<T>, DataManager<T>> getDataManagerMap() {
        return (Map) c2DataManagers.computeIfAbsent(this.getClass(), (k) -> new HashMap<>());
    }

    /**
     * Adds a local data manager with the given key.
     *
     * @param key the key.
     * @param dataClass the data class.
     * @return the data manager
     * @param <T> the type
     */
    default <T extends DataSerializable> DataManager<T> addDataManager(String key, Class<T> dataClass) {
        return addDataManager(key, dataClass, false);
    }

    /**
     * Adds a data manager with the given key and data class to this module's data manager map
     *
     * @param key the key of the data manager
     * @param dataClass the data class
     * @param mongo if a remote data manager should be used
     * @param <T> the type of the data
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default <T extends DataSerializable> DataManager<T> addDataManager(String key, Class<T> dataClass, boolean mongo) {
        DataManager<T> local = mongo ? new DataManagerRemote<>(key, JavaPlugin.getProvidingPlugin(getClass()), dataClass) : new DataManagerLocal<>(key, JavaPlugin.getProvidingPlugin(getClass()), dataClass);
        Map c2DataMap = c2DataManagers.computeIfAbsent(getClass(), (k) -> new HashMap<>());
        Map dataMap = dataManagers.computeIfAbsent(getClass(), (k) -> new HashMap<>());
        c2DataMap.put(dataClass, local);
        dataMap.put(key, local);
        return local;
    }

    /**
     * Clears the backing data maps for this data manager.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    default void shutdownDataManagers() {
        Map c2DataMap = c2DataManagers.computeIfAbsent(getClass(), (k) -> new HashMap<>());
        Map dataMap = dataManagers.computeIfAbsent(getClass(), (k) -> new HashMap<>());
        c2DataMap.values().forEach(obj -> {
            ((DataManager) obj).shutdown();
        });

        c2DataMap.clear();
        dataMap.clear();
    }

    /**
     * Gets the data manager associated with the given data class.
     *
     * @param dataClass the data class.
     * @return the data manager, or null.
     * @param <T> the type of data serializable.
     */
    @SuppressWarnings("unchecked")
    default <T extends DataSerializable> DataManager<T> getDataManager(Class<T> dataClass) {
        return (DataManager<T>) getDataManagerMap().get(dataClass);
    }

    /**
     * Gets, or creates, the object of the given type linked to the given key.
     *
     * @param key the key.
     * @param type the type.
     * @return the data.
     * @param <T> the type of the data.
     */
    default <T extends DataSerializable> T getOrCreate(String key, Class<T> type) {
        DataManager<T> dataManager = getDataManager(type);
        if(dataManager == null)
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", type.getName()));

        if (!dataExists(key, type)) {
            return dataManager.getOrCreate(key);
        }
        return dataManager.getObject(key);
    }

    /**
     * Gets, or creates, the object of the given type linked to the given key.
     *
     * @param type the type.
     * @param key the key.
     * @return the data.
     * @param <T> the type of the data.
     */
    default <T extends DataSerializable> T getOrCreate(UUID key, Class<T> type) {
        return getOrCreate(key.toString(), type);
    }

    /**
     * Generates a new unique data key for the given data class.
     *
     * @param dataClass the data class.
     * @return the new unique key.
     * @param <T> the type of data serializable.
     */
    default <T extends DataSerializable> UUID getUniqueDataKey(Class<T> dataClass) {
        DataManager<T> dataManager = getDataManager(dataClass);
        if(dataManager == null)
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", dataClass.getName()));

        return dataManager.getUniqueKey();
    }

    /**
     * Saves the given data to its respective data class
     *
     * @param data the data to save.
     * @param <T> the type of data
     */
    @SuppressWarnings("unchecked")
    default <T extends DataSerializable> void saveData(T data) {
        if(!getDataManagerMap().containsKey(data.getClass()))
            throw new UnsupportedOperationException(String.format("Data with class %s not supported!", data.getClass().getName()));

        DataManager<T> dataManager = (DataManager<T>) getDataManager(data.getClass());
        dataManager.store(data);
    }

    //Shorthand for the below method.
    default <T extends DataSerializable> void deleteData(UUID key, Class<T> dataClass) {
        this.deleteData(key.toString(), dataClass);
    }

    /**
     * Deletes the given key with its data.
     *
     * @param key the data key.
     * @param <T> the type of the data.
     */
    default <T extends DataSerializable> void deleteData(String key, Class<T> dataClass) {
        if(!this.getDataManagerMap().containsKey(dataClass))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", dataClass.getName()));

        DataManager<T> dataManager = getDataManager(dataClass);
        dataManager.delete(key);
    }

    /**
     * Deletes the given object.
     *
     * @param data the data.
     * @param <T> the type of the data.
     */
    @SuppressWarnings("unchecked")
    default <T extends DataSerializable> void deleteData(T data) {
        if(!this.getDataManagerMap().containsKey(data.getClass()))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", data.getClass().getName()));

        DataManager<T> dataManager = (DataManager<T>) getDataManager(data.getClass());
        dataManager.delete(data.getKey());
    }

    //Shorthand for the below method.
    default <T extends DataSerializable> boolean dataExists(UUID key, Class<T> dataClass) {
        return this.dataExists(key.toString(), dataClass);
    }

    /**
     * Checks if the given data with the key exists for the data class.
     *
     * @param key the key of the data
     * @param dataClass the data class.
     * @return true if it exists, or false if it doesn't
     * @param <T> the type of data.
     */
    default <T extends DataSerializable> boolean dataExists(String key, Class<T> dataClass) {
        if(!this.getDataManagerMap().containsKey(dataClass))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", dataClass.getName()));

        DataManager<T> dataManager = getDataManager(dataClass);
        return dataManager.exists(key);
    }

    //Shorthand for the below method.
    @Nullable
    default <T extends DataSerializable> T loadData(UUID key, Class<T> type) {
        return this.loadData(key.toString(), type);
    }

    /**
     * Gets the given data with the given key from the data manager of its type.
     *
     * @return the loaded object, or null if it didn't exist
     * @param <T> the type of data
     */
    @Nullable
    default <T extends DataSerializable> T loadData(String key, Class<T> type) {
        if(!this.getDataManagerMap().containsKey(type))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", type.getName()));

        return getDataManager(type).getObject(key);
    }

    /**
     * Gets all the data instances for the given data type.
     *
     * @param type the type.
     * @return a collection of all the data.
     * @param <T> the type of data.
     */
    default  <T extends DataSerializable> Collection<T> getAllDataOfType(Class<T> type) {
        if(!this.getDataManagerMap().containsKey(type))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", type.getName()));

        DataManager<T> dataManager = getDataManager(type);

        return Collections.unmodifiableCollection(dataManager.getAll().values());
    }

    /**
     * Gets all the cached data instances for the given data type.
     *
     * @param type the type.
     * @return a collection of all the data.
     * @param <T> the type of data.
     */
    default  <T extends DataSerializable> Collection<T> getAllCachedDataOfType(Class<T> type) {
        if(!this.getDataManagerMap().containsKey(type))
            throw new UnsupportedOperationException(String.format("Data class %s not supported!", type.getName()));

        DataManager<T> dataManager = getDataManager(type);

        return Collections.unmodifiableCollection(dataManager.getAllCached().values());
    }
}
