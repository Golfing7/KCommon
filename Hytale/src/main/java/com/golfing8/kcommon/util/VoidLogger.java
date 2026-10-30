package com.golfing8.kcommon.util;

import com.golfing8.kcommon.nms.reflection.FieldHandle;
import com.golfing8.kcommon.nms.reflection.FieldHandles;
import com.google.common.flogger.AbstractLogger;
import com.google.common.flogger.backend.LogData;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.logger.backend.HytaleLoggerBackend;
import org.jspecify.annotations.NonNull;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Silences Hytale loggers.
 */
public final class VoidLogger {
    private VoidLogger() {
    }

    /**
     * Makes the given logger drop everything it logs.
     *
     * @param logger the logger
     */
    public static void shutUp(HytaleLogger logger) {
        FieldHandle<HytaleLoggerBackend> backend = FieldHandles.getHandle("backend", HytaleLogger.class);
        FieldHandle<HytaleLoggerBackend> backendParent = FieldHandles.getHandle("backend", AbstractLogger.class);
        backend.set(logger, NoOp.INSTANCE);
        backendParent.set(logger, NoOp.INSTANCE);

        // Test it.
        logger.at(Level.INFO).log("This shouldn't occur.");
    }

    /**
     * Silences a class's static {@code LOGGER} field, if it has one.
     *
     * @param clazz the class
     */
    public static void shutUpClass(Class<?> clazz) {
        FieldHandles.getHandleOpt("LOGGER", clazz).ifPresent(f -> {
            shutUp((HytaleLogger) f.get(null));
        });
    }

    private static class NoOp extends HytaleLoggerBackend {
        public static final NoOp INSTANCE = new NoOp("NOOP");

        protected NoOp(String name) {
            super(name);
        }

        @Override
        public void log(@NonNull LogData data) {

        }

        @Override
        public void log(@NonNull LogRecord logRecord) {

        }

        @Override
        public void log(@NonNull LogRecord logRecord, boolean sentryHandled) {

        }

        @Override
        public boolean isLoggable(@NonNull Level lvl) {
            return false;
        }
    }
}
