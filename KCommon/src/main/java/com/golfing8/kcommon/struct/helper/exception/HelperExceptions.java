/*
 * This file is part of helper, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.golfing8.kcommon.struct.helper.exception;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.helper.exception.events.HelperExceptionEvent;
import com.golfing8.kcommon.struct.helper.exception.types.EventHandlerException;
import com.golfing8.kcommon.struct.helper.exception.types.PromiseChainException;
import com.golfing8.kcommon.struct.helper.exception.types.SchedulerTaskException;
import com.golfing8.kcommon.struct.helper.interfaces.Delegate;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

/**
 * Central handler for exceptions that occur within user-written
 * Runnables and handlers running in helper.
 */
public final class HelperExceptions {
    private HelperExceptions() {
    }

    private static final ThreadLocal<AtomicBoolean> NOT_TODAY_STACK_OVERFLOW_EXCEPTION =
            ThreadLocal.withInitial(() -> new AtomicBoolean(false));

    private static void log(InternalException exception) {
        // print to logger
        KCommon.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);

        // call event
        AtomicBoolean firing = NOT_TODAY_STACK_OVERFLOW_EXCEPTION.get();
        if (firing.compareAndSet(false, true)) {
            try {
                Bukkit.getServer().getPluginManager().callEvent(new HelperExceptionEvent(exception));
            } finally {
                firing.set(false);
            }
        }
    }

    /**
     * Reports a scheduler exception
     *
     * @param throwable the causing throwable
     */
    public static void reportScheduler(Throwable throwable) {
        log(new SchedulerTaskException(throwable));
    }

    /**
     * Reports a promise exception
     *
     * @param throwable the causing throwable
     */
    public static void reportPromise(Throwable throwable) {
        log(new PromiseChainException(throwable));
    }

    /**
     * Reports an event exception
     *
     * @param throwable the causing throwable
     */
    public static void reportEvent(Object event, Throwable throwable) {
        log(new EventHandlerException(throwable, event));
    }

    /**
     * Wraps a runnable into a scheduler runnable
     *
     * @param runnable the runnable
     * @return the new runnable
     */
    public static Runnable wrapSchedulerTask(Runnable runnable) {
        return new SchedulerWrappedRunnable(runnable);
    }

    private static final class SchedulerWrappedRunnable implements Runnable, Delegate<Runnable> {
        private final Runnable delegate;

        private SchedulerWrappedRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            try {
                this.delegate.run();
            } catch (Throwable t) {
                reportScheduler(t);
            }
        }

        @Override
        public Runnable getDelegate() {
            return this.delegate;
        }
    }

}
