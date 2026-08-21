package com.golfing8.kcommon.config.adapter;

import com.golfing8.kcommon.config.ConfigTypeRegistry;
import com.golfing8.kcommon.config.exc.InvalidConfigException;
import com.golfing8.kcommon.struct.drop.CommandDrop;
import com.golfing8.kcommon.struct.drop.Drop;
import com.golfing8.kcommon.struct.drop.XpDrop;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Note: the "items"/"item" branch of {@link com.golfing8.kcommon.config.adapter.CADrop} deserializes
 * an {@link com.golfing8.kcommon.struct.item.ItemStackBuilder}, which requires a live Bukkit ItemStack.
 * That branch is covered separately by whichever test suite exercises Bukkit-dependent config adapters.
 */
class CADropTest {

    @Test
    @DisplayName("Round trips an XpDrop")
    void testRoundTripsXpDrop() {
        XpDrop original = new XpDrop(50.0, "Bonus XP", 2.0, 100, true, false);
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Drop<?> result = ConfigTypeRegistry.getFromType(primitive, Drop.class);

        assertTrue(result instanceof XpDrop);
        XpDrop xpResult = (XpDrop) result;
        assertEquals(50.0, xpResult.getChance());
        assertEquals("Bonus XP", xpResult.getDisplayName());
        assertEquals(100, xpResult.getXp());
        assertEquals(true, xpResult.isBoostQuantity());
        assertEquals(false, xpResult.isGiveDirectly());
    }

    @Test
    @DisplayName("Round trips a CommandDrop")
    void testRoundTripsCommandDrop() {
        CommandDrop original = new CommandDrop(75.0, null, 1.0, Arrays.asList("give {PLAYER} diamond 1"));
        ConfigPrimitive primitive = ConfigTypeRegistry.toPrimitive(original);
        Drop<?> result = ConfigTypeRegistry.getFromType(primitive, Drop.class);

        assertTrue(result instanceof CommandDrop);
        CommandDrop commandResult = (CommandDrop) result;
        assertEquals(75.0, commandResult.getChance());
        List<String> commands = commandResult.getCommands();
        assertEquals(1, commands.size());
        assertEquals("give {PLAYER} diamond 1", commands.get(0));
    }

    @Test
    @DisplayName("A definition with neither items/item, commands, nor xp keys throws")
    void testUnrecognizedDropDefinitionThrows() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("chance", 100.0);
        ConfigPrimitive primitive = ConfigPrimitive.ofMap(map);

        assertThrows(InvalidConfigException.class,
                () -> ConfigTypeRegistry.getFromType(primitive, Drop.class));
    }
}
