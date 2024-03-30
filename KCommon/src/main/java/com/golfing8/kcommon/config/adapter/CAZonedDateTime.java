package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.reflection.FieldType;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class CAZonedDateTime implements ConfigAdapter<ZonedDateTime> {
    @Override
    public Class<ZonedDateTime> getAdaptType() {
        return ZonedDateTime.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ZonedDateTime toPOJO(ConfigPrimitive entry, FieldType type) {
        if (entry.getPrimitive() == null)
            return null;

        Map<String, Object> map = (Map<String, Object>) entry.getPrimitive();
        ZoneId zone = map.containsKey("zone-id") ? ZoneId.of(map.get("zone-id").toString()) : KCommon.getInstance().getTimeZone();
        int year = (int) map.get("year");
        int month = (int) map.get("month");
        int day = (int) map.get("day");
        int hour = (int) map.getOrDefault("hour", 0);
        int minute = (int) map.getOrDefault("minute", 0);
        int second = (int) map.getOrDefault("second", 0);
        return ZonedDateTime.of(year, month, day, hour, minute, second, 0, zone);
    }

    @Override
    public ConfigPrimitive toPrimitive(@NotNull ZonedDateTime object) {
        Map<String, Object> primitive = new LinkedHashMap<>();
        primitive.put("zone-id", object.getZone().getId());
        primitive.put("year", object.getYear());
        primitive.put("month", object.getMonthValue());
        primitive.put("day", object.getDayOfMonth());
        primitive.put("hour", object.getHour());
        primitive.put("minute", object.getMinute());
        primitive.put("second", object.getSecond());
        return ConfigPrimitive.ofMap(primitive);
    }
}
