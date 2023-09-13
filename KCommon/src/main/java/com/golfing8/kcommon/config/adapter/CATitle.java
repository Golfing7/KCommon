package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.struct.reflection.FieldType;
import com.golfing8.kcommon.struct.title.Title;

import java.util.HashMap;
import java.util.Map;

public class CATitle implements ConfigAdapter<Title> {
    @Override
    public Class<Title> getAdaptType() {
        return Title.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Title toPOJO(ConfigPrimitive entry, FieldType type) {
        Map<String, Object> items = (Map<String, Object>) entry.getPrimitive();
        if (items == null)
            return null;

        String title = (String) items.get("title");
        String subTitle = (String) items.get("subtitle");
        int inTime = (Integer) items.get("in");
        int stayTime = (Integer) items.get("stay");
        int outTime = (Integer) items.get("out");
        return new Title(title, subTitle, inTime, stayTime, outTime);
    }

    @Override
    public ConfigPrimitive toPrimitive(Title title) {
        Map<String, Object> items = new HashMap<>();
        items.put("title", title.getTitle());
        items.put("subtitle", title.getSubtitle());
        items.put("in", title.getIn());
        items.put("stay", title.getStay());
        items.put("out", title.getOut());
        return ConfigPrimitive.ofMap(items);
    }
}
