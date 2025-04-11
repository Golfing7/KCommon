package com.golfing8.kcommon.config.adapter.xseries;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import com.golfing8.kcommon.config.adapter.ConfigAdapter;
import com.golfing8.kcommon.config.adapter.ConfigPrimitive;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

/**
 * A config adapter for {@link XEnchantment}s. This is needed as XEnchantment provides some lookup methods differing from the typical {@link Enum#valueOf}
 */
public class CAXEnchantment implements ConfigAdapter<XEnchantment> {
    @Override
    public Class<XEnchantment> getAdaptType() {
        return XEnchantment.class;
    }

    @Override
    public XEnchantment toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        return XEnchantment.of(entry.getPrimitive().toString()).orElse(null);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull XEnchantment object) {
        return ConfigPrimitive.ofString(object.name());
    }
}
