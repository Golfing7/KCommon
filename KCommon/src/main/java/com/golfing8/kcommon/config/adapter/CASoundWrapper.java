package com.golfing8.kcommon.config.adapter;

import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.struct.SoundWrapper;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CASoundWrapper implements ConfigAdapter<SoundWrapper> {
    @Override
    public Class<SoundWrapper> getAdaptType() {
        return SoundWrapper.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SoundWrapper toPOJO(ConfigPrimitive entry, FieldType type) {
        Map<String, Object> items = (Map<String, Object>) entry.getPrimitive();
        if (items == null)
            return null;

        Optional<XSound> xSoundOptional = XSound.matchXSound(items.get("sound").toString());
        if(!xSoundOptional.isPresent())
            throw new ImproperlyConfiguredValueException(entry.getSource(), "sound");

        double pitch = (Double) items.getOrDefault("pitch", 1.0D);
        double volume = (Double) items.getOrDefault("volume", 1.0D);
        int delay = (Integer) items.getOrDefault("delay", 0);
        return new SoundWrapper(xSoundOptional.get(), (float) volume, (float) pitch, delay);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull SoundWrapper wrapper) {
        Map<String, Object> items = new HashMap<>();
        items.put("sound", wrapper.getSound().name());
        items.put("pitch", wrapper.getPitch());
        items.put("volume", wrapper.getVolume());
        items.put("delay", wrapper.getDelay());
        return ConfigPrimitive.ofMap(items);
    }
}
