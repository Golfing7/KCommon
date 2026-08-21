package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.struct.filter.ItemFilter;
import com.golfing8.kcommon.struct.filter.StringFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CAItemFilterTest {

    @Test
    @DisplayName("Round trips material/name/lore filters and strip-colors")
    void testRoundTripsItemFilter() {
        ItemFilter original = new ItemFilter(
                com.google.common.collect.Sets.newHashSet(new StringFilter("DIAMOND")),
                com.google.common.collect.Sets.newHashSet(new StringFilter("Cool Item")),
                com.google.common.collect.Sets.newHashSet(new StringFilter("Rare")),
                true
        );

        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        ItemFilter result = ConfigTypeRegistry.getFromType(primitive, ItemFilter.class);

        assertEquals(1, result.getMaterialFilters().size());
        assertEquals("DIAMOND", result.getMaterialFilters().iterator().next().getPattern());
        assertEquals(1, result.getItemNameFilters().size());
        assertEquals(1, result.getItemLoreFilters().size());
        assertTrue(result.isStripColors());
    }

    @Test
    @DisplayName("Empty filter sets are omitted from serialization")
    void testEmptyFilterSetsAreOmitted() {
        ItemFilter original = new ItemFilter(null, null, null, false);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);

        Map<String, Object> unwrapped = primitive.unwrap();
        assertFalse(unwrapped.containsKey("material-filters"));
        assertFalse(unwrapped.containsKey("name-filters"));
        assertFalse(unwrapped.containsKey("lore-filters"));
        assertEquals(false, unwrapped.get("strip-colors"));
    }

    @Test
    @DisplayName("Deserializing with no filter keys yields empty filter sets")
    void testDeserializeWithNoFilterKeys() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("strip-colors", false);
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        ItemFilter result = ConfigTypeRegistry.getFromType(primitive, ItemFilter.class);
        assertTrue(result.getMaterialFilters().isEmpty());
        assertTrue(result.getItemNameFilters().isEmpty());
        assertTrue(result.getItemLoreFilters().isEmpty());
    }
}
