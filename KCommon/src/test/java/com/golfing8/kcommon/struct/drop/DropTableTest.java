package com.golfing8.kcommon.struct.drop;

import com.golfing8.kcommon.struct.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DropTableTest {

    /**
     * A deterministic test double: never touches Bukkit, and always/never "hits" based on chance.
     */
    static class TestDrop extends Drop<String> {
        TestDrop(double chance) {
            super(chance, "test-drop");
        }

        @Override
        public List<String> getDrop() {
            return Collections.singletonList("value");
        }

        @Override
        public void giveTo(DropContext context) {
            // no-op, not exercised here
        }
    }

    private static final double ALWAYS = 1000.0;
    private static final double NEVER = -1.0;

    @Nested
    @DisplayName("generateDrops")
    class GenerateDrops {
        @Test
        @DisplayName("Includes drops whose chance always succeeds")
        void testAlwaysIncluded() {
            Map<String, Drop<?>> drops = new HashMap<>();
            drops.put("always", new TestDrop(ALWAYS));
            DropTable table = new DropTable(drops);

            List<Drop<?>> generated = table.generateDrops();
            assertEquals(1, generated.size());
        }

        @Test
        @DisplayName("Excludes drops whose chance always fails")
        void testNeverIncluded() {
            Map<String, Drop<?>> drops = new HashMap<>();
            drops.put("never", new TestDrop(NEVER));
            DropTable table = new DropTable(drops);

            List<Drop<?>> generated = table.generateDrops();
            assertTrue(generated.isEmpty());
        }

        @Test
        @DisplayName("A drop target range caps the number of collected drops from a group")
        void testDropTargetCapsCollectedDrops() {
            Map<String, Drop<?>> drops = new HashMap<>();
            drops.put("a", new TestDrop(ALWAYS));
            drops.put("b", new TestDrop(ALWAYS));
            drops.put("c", new TestDrop(ALWAYS));
            // Only ever take exactly 1 from the default group.
            DropTable table = new DropTable(drops, new Range(1, 1));

            List<Drop<?>> generated = table.generateDrops();
            assertEquals(1, generated.size());
        }

        @Test
        @DisplayName("An empty table produces no drops")
        void testEmptyTable() {
            DropTable table = new DropTable(Collections.emptyMap());
            assertTrue(table.generateDrops().isEmpty());
        }

        @Test
        @DisplayName("A zero boost prevents any positive-chance drop from succeeding")
        void testZeroBoostPreventsDrops() {
            Map<String, Drop<?>> drops = new HashMap<>();
            drops.put("a", new TestDrop(50.0));
            DropTable table = new DropTable(drops);

            DropContext zeroBoost = new DropContext(null, 0.0, Collections.emptyMap());
            List<Drop<?>> generated = table.generateDrops(zeroBoost);
            assertTrue(generated.isEmpty(), "A zero boost should never allow a positive-chance drop to succeed");
        }

        @Test
        @DisplayName("A drop's maxBoost caps the effective boost applied from the context")
        void testMaxBoostClamps() {
            // With a fixed "random" roll of 0.5 (i.e. the 50% mark), chance=40 & maxBoost=1 means the
            // clamped effective roll is 40*1=40, which is < 50 and so never succeeds -- but if the huge
            // context boost were applied unclamped (40*100_000), it would trivially always succeed.
            Map<String, Drop<?>> drops = new HashMap<>();
            drops.put("clamped", new TestDropWithMaxBoost(40.0, 1.0));
            DropTable table = new DropTable(drops);

            DropContext hugeBoost = new DropContext(null, 100_000.0, Collections.emptyMap());
            assertTrue(table.generateDrops(hugeBoost).isEmpty(),
                    "maxBoost should have clamped the huge context boost");
        }
    }

    /**
     * A drop whose "random roll" is fixed at exactly the halfway point (50/100), making
     * {@link RandomTestable#testRandom(double)} fully deterministic for a given chance/boost.
     */
    static class TestDropWithMaxBoost extends Drop<String> {
        TestDropWithMaxBoost(double chance, double maxBoost) {
            super(chance, "test-drop", maxBoost);
        }

        @Override
        public java.util.Random getRandomInstance() {
            return new java.util.Random() {
                @Override
                public double nextDouble() {
                    return 0.5;
                }
            };
        }

        @Override
        public List<String> getDrop() {
            return Collections.singletonList("value");
        }

        @Override
        public void giveTo(DropContext context) {
        }
    }
}
