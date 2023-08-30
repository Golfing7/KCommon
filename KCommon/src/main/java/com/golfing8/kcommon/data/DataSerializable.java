package com.golfing8.kcommon.data;

/**
 * Represents something that can be serialized by a {@link DataManager}.
 */
public interface DataSerializable extends JsonSerializable {
    /**
     * Gets the key to use for this serializable
     *
     * @return the key
     */
    String getKey();

    /**
     * Sets the serialization key for this object.
     *
     * @param key the key.
     */
    void setKey(String key);

    /**
     * Marks this data as 'changed'. Data that has 'changed' will be autosaved.
     */
    void change();

    /**
     * Marks this object as having been saved.
     */
    void markSaved();

    /**
     * If this object has changed.
     *
     * @return if the object has been changed.
     */
    boolean hasChanged();
}
