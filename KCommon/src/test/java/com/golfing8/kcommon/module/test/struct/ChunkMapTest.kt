package com.golfing8.kcommon.module.test.struct

import com.golfing8.kcommon.nms.struct.Position
import com.golfing8.kcommon.struct.map.ChunkStylePointMap
import com.golfing8.kcommon.struct.map.UnboundedCSPointMap
import io.netty.util.internal.ThreadLocalRandom
import org.junit.jupiter.api.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChunkMapTest {
    @Test
    fun `test unbounded chunk map static`() {
        val values = mapOf(
            Position(-5109, 50, 14815) to 0,
            Position(-59, 1, -15) to 1,
            Position(6234, -23, 892) to 2,
            Position(1058, 250, -5) to 3,
            Position(100, 6, -508) to 4,
            Position(0, 0, 0) to 5
        )
        testValues(values, UnboundedCSPointMap())
    }

    @Test
    fun `test unbounded chunk map dynamic`() {
        val values = mutableMapOf<Position, Int>()
        (0..15).forEach {
            values += Position(ThreadLocalRandom.current().nextInt(-15000, 15000),
                ThreadLocalRandom.current().nextInt(-64, 321),
                ThreadLocalRandom.current().nextInt(-15000, 15000)) to it
        }
        testValues(values, UnboundedCSPointMap())
    }

    private fun testValues(values: Map<Position, Int>, chunkMap: ChunkStylePointMap<Int>) {
        // Place in some values
        for (pair in values) {
            chunkMap[pair.key] = pair.value
        }

        // Ensure that all values are properly mapped
        for (pair in values) {
            assertEquals(pair.value, chunkMap[pair.key], "Map lookup should match")
        }

        // Ensure iterator works properly
        var matched = 0
        for (value in chunkMap.values) {
            assertTrue { values.containsValue(value) }
            matched++
        }
        assertEquals(values.size, matched, "Should match every value")

        // Ensure entries work
        for (entry in chunkMap.entries) {
            assertContains(values, entry.key, "Should give correct entries")
            assertEquals(values[entry.key], entry.value)
        }
    }
}