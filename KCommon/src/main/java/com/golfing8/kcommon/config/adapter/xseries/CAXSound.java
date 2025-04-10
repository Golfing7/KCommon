package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XEntityType;
import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XSound}s. This is needed as XSound provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXSound implements ConfigAdapter<XSound> {
    @Override
    public Class<XSound> getAdaptType() {
        return XSound.class;
    }

    @Override
    public XSound toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XSound.matchXSound(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XSound object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
