package com.golfing8.kcommon.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public abstract class DataManagerAbstract<T extends DataSerializable> implements DataManager<T>{
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Getter
    private final String key;
    @Getter
    private final Plugin plugin;
    @Getter
    private final Class<T> typeClass;
    private final Constructor<T> typeConstructor;

    public DataManagerAbstract(String key, Plugin plugin, Class<T> typeClass) {
        this.key = key;
        this.plugin = plugin;
        this.typeClass = typeClass;

        try{
            typeConstructor = typeClass.getDeclaredConstructor();
            typeConstructor.setAccessible(true);
        }catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Failed to find default constructor for %s class!", typeClass.getName()), e);
        }
    }

    /**
     * Gets the GSON instance this data manager uses.
     *
     * @return the gson instance.
     */
    public Gson getGSON() {
        return GSON;
    }

    @Override
    public UUID getUniqueKey() {
        UUID newUUID = UUID.randomUUID();
        while(exists(newUUID.toString()))
            newUUID = UUID.randomUUID();
        return newUUID;
    }

    /**
     * Creates an empty instance of the class this data manager uses
     *
     * @return the empty instance
     */
    protected T createEmpty() {
        try{
            return typeConstructor.newInstance();
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(String.format("Failed to instantiate empty instance of class %s!", typeClass.getName()), e);
        }
    }
}
