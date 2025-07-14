package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XPotion;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XPotion}s. This is needed as XPotion provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXPotion implements ConfigAdapter<XPotion> {
    @Override
    public Class<XPotion> getAdaptType() {
        return XPotion.class;
    }

    @Override
    public XPotion toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XPotion.of(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XPotion object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
