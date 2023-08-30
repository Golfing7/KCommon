package com.golfing8.kcommon.data.local;

import com.golfing8.kcommon.data.DataSerializable;
import com.golfing8.kcommon.data.key.FieldIndexer;
import com.golfing8.kcommon.util.MapUtil;
import com.golfing8.kcommon.util.Reflection;
import com.golfing8.kcommon.nms.reflection.FieldHandle;

import java.util.*;

/**
 * A local implementation of the {@link FieldIndexer} interface.
 * <p>
 * Loads all instances available and indexes them.
 * </p>
 * @param <T>
 */
public class FieldIndexerLocal<T extends DataSerializable> implements FieldIndexer<T> {
    /** The data manager this keying manager works with. */
    private final DataManagerLocal<T> dataManager;
    /** Stores a handle to all fields of a class */
    private final Map<String, FieldHandle<?>> fieldHandleMap;

    public FieldIndexerLocal(DataManagerLocal<T> local) {
        this.dataManager = local;
        this.fieldHandleMap = Reflection.getAllFields(getIndexClass());
    }

    @Override
    public Class<T> getIndexClass() {
        return this.dataManager.getTypeClass();
    }

    @Override
    public List<T> getWhere(String field, Object value, Object... keyValues) {
        Collection<T> all = dataManager.getAll().values();
        List<T> toReturn = new ArrayList<>();
        Map<String, Object> map = MapUtil.of(field, value, keyValues);
        for (Map.Entry<String, Object> lookupEntry : map.entrySet()) {
            FieldHandle<?> expectedFieldHandle = fieldHandleMap.get(lookupEntry.getKey());
            if (expectedFieldHandle == null)
                return Collections.emptyList();

            Object expected = lookupEntry.getValue();
            for (T obj : all) {
                Object fieldValue = expectedFieldHandle.get(obj);
                if (!Objects.equals(fieldValue, expected))
                    continue;

                toReturn.add(obj);
            }
        }
        return toReturn;
    }
}
