package com.golfing8.kcommon.config.adapter;

import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XMaterial}s. This is needed as XMaterial provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXMaterial implements ConfigAdapter<XMaterial> {
    @Override
    public Class<XMaterial> getAdaptType() {
        return XMaterial.class;
    }

    @Override
    public XMaterial toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XMaterial.matchXMaterial(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XMaterial object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
