package com.golfing8.kcommon.module.test;

import com.golfing8.kcommon.data.serializer.DataSerializer;
import com.golfing8.kcommon.module.test.util.FakeServer;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;

public class ItemStackSerializationTest {
    @Test
    public void testSerializationSimple() {
        FakeServer.getServer();
        ItemStack itemStack = new ItemStack(Material.ANVIL);
    }
}
