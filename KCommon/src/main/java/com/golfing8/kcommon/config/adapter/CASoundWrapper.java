package com.golfing8.kcommon.config.adapter;

import com.cryptomorin.xseries.XSound;
import com.golfing8.kcommon.config.ImproperlyConfiguredValueException;
import com.golfing8.kcommon.config.lang.Message;
import com.golfing8.kcommon.struct.SoundWrapper;

import java.util.HashMap;
import java.util.List;
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

        double pitch = (Double) items.get("pitch");
        double volume = (Double) items.get("volume");
        int delay = (Integer) items.get("delay");
        return new SoundWrapper(xSoundOptional.get(), (float) volume, (float) pitch, delay);
    }

    @Override
    public ConfigPrimitive toPrimitive(SoundWrapper wrapper) {
        Map<String, Object> items = new HashMap<>();
        items.put("sound", wrapper.getSound().name());
        items.put("pitch", wrapper.getPitch());
        items.put("volume", wrapper.getVolume());
        items.put("delay", wrapper.getDelay());
        return ConfigPrimitive.ofMap(items);
    }
}
