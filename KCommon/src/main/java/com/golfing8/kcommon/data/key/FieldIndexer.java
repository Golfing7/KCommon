package com.golfing8.kcommon.data.key;


import com.golfing8.kcommon.data.DataSerializable;

import java.util.List;

/**
 * Indexes fields for a given type of class.
 */
public interface FieldIndexer<T extends DataSerializable> {
    /**
     * The class this indexer is keeping index of.
     *
     * @return the index class.
     */
    Class<T> getIndexClass();

    /**
     * Gets all objects where the given field value matches.
     *
     * @param field     the field's name.
     * @param value     the value.
     * @param keyValues all key value pairs.
     * @return all applicable objects.
     */
    List<T> getWhere(String field, Object value, Object... keyValues);
}
