package com.golfing8.kcommon.data.local;

import com.golfing8.kcommon.KPluginContext;
import com.golfing8.kcommon.data.AbstractSerializable;
import com.golfing8.kcommon.db.MongoConnector;
import com.golfing8.kcommon.db.redis.RedisAdapter;
import com.golfing8.kcommon.platform.Platform;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DataManagerLocalTest {
    @TempDir
    Path pluginFolder;

    public static class Stored extends AbstractSerializable {
        int value;
    }

    private KPluginContext plugin;

    @BeforeEach
    void setUp() {
        plugin = new KPluginContext() {
            @Override
            public String getPluginName() {
                return "Test";
            }

            @Override
            public Logger getPluginLogger() {
                return Logger.getAnonymousLogger();
            }

            @Override
            public Path getPluginDataFolder() {
                return pluginFolder;
            }

            @Override
            public Path getPluginJar() {
                return pluginFolder;
            }
        };
        Platform.set(new Platform() {
            public long getCurrentTick() {
                return 0;
            }

            public void sendConsoleMessage(String message) {
            }

            public boolean supportsHexColors() {
                return true;
            }

            public Logger getLogger() {
                return Logger.getAnonymousLogger();
            }

            public ZoneId getTimeZone() {
                return ZoneId.systemDefault();
            }

            public KPluginContext getKCommon() {
                return plugin;
            }

            public KPluginContext getProvidingPlugin(Class<?> clazz) {
                return plugin;
            }

            public MongoConnector getMongoConnector() {
                return null;
            }

            public RedisAdapter getRedisAdapter() {
                return RedisAdapter.noop();
            }

            public boolean isMainThread(Thread thread) {
                return true;
            }

            public void runSync(Runnable task) {
                task.run();
            }

            public void runAsync(Runnable task) {
                task.run();
            }

            public Terminable runLater(Runnable task, long delayTicks) {
                return () -> {
                };
            }

            public Terminable runLaterAsync(Runnable task, long delayTicks) {
                return () -> {
                };
            }

            public Terminable runTimer(Runnable task, long delayTicks, long periodTicks) {
                return () -> {
                };
            }

            public Terminable runTimerAsync(Runnable task, long delayTicks, long periodTicks) {
                return () -> {
                };
            }
        });
    }

    private static String read(Path path) throws Exception {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private Path folder() {
        return pluginFolder.resolve("data").resolve("stored");
    }

    @Test
    void storeWritesTheObjectWithoutLeavingATemporaryFile() throws Exception {
        DataManagerLocal<Stored> manager = new DataManagerLocal<>("stored", plugin, Stored.class);
        Stored stored = manager.getOrCreate("a");
        stored.value = 5;
        manager.store(stored);

        assertTrue(read(folder().resolve("a.json")).contains("\"value\": 5"));
        assertFalse(Files.exists(folder().resolve("a.json.tmp")));
    }

    @Test
    void unreadableObjectIsSetAsideInsteadOfDeleted() throws Exception {
        DataManagerLocal<Stored> manager = new DataManagerLocal<>("stored", plugin, Stored.class);
        Files.write(folder().resolve("broken.json"), "[1, 2, 3]".getBytes(StandardCharsets.UTF_8));

        assertNull(manager.getObject("broken"));
        assertFalse(Files.exists(folder().resolve("broken.json")));
        try (Stream<Path> files = Files.list(folder())) {
            Path setAside = files.filter(p -> p.getFileName().toString().startsWith("broken.json.corrupt-")).findFirst().orElseThrow(AssertionError::new);
            assertEquals("[1, 2, 3]", read(setAside));
        }

        // Set aside files aren't loaded as objects.
        manager.getOrCreate("good");
        manager.save("good");
        assertEquals(1, manager.getAll().size());
    }
}
