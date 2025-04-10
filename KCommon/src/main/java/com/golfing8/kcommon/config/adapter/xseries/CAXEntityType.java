package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XEntityType}s. This is needed as XEntityType provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXEntityType implements ConfigAdapter<XEntityType> {
    @Override
    public Class<XEntityType> getAdaptType() {
        return XEntityType.class;
    }

    @Override
    public XEntityType toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XEntityType.of(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XEntityType object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
