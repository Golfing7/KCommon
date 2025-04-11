package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XBiome;
import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XBiome}s. This is needed as XBiome provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXBiome implements ConfigAdapter<XBiome> {
    @Override
    public Class<XBiome> getAdaptType() {
        return XBiome.class;
    }

    @Override
    public XBiome toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XBiome.of(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XBiome object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
