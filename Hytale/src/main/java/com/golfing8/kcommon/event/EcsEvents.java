package com.golfing8.kcommon.event;

import com.golfing8.kcommon.KCommon;
import com.golfing8.kcommon.struct.helper.exception.HelperExceptions;
import com.golfing8.kcommon.struct.helper.terminable.Terminable;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lets modules listen to entity ECS events and stop listening again.
 * <p>
 * Hytale only removes ECS systems when their plugin shuts down, so a module can't register its own system
 * and still be disabled and reloaded. Instead KCommon registers one system per event type, the first time
 * anything listens to it, and passes each event to the handlers registered at that moment.
 * </p>
 */
public final class EcsEvents {
    private static final Map<Class<?>, List<Handler<?>>> HANDLERS = new ConcurrentHashMap<>();
    private static final Set<Class<?>> REGISTERED = ConcurrentHashMap.newKeySet();

    private EcsEvents() {
    }

    /**
     * Handles an entity ECS event.
     *
     * @param <E> the event type.
     */
    @FunctionalInterface
    public interface Handler<E extends EcsEvent> {
        /**
         * Handles the event.
         *
         * @param entity the entity the event happened to, such as the player breaking a block or the entity taking damage.
         * @param store  the store the entity lives in.
         * @param buffer the command buffer for changes to the store.
         * @param event  the event.
         */
        void handle(Ref<EntityStore> entity, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, E event);
    }

    /**
     * Starts passing events of the given type to the handler.
     *
     * @param eventClass the event class.
     * @param handler    the handler.
     * @param <E>        the event type.
     * @return a handle that stops the handler when closed.
     */
    public static <E extends EcsEvent> Terminable listen(Class<E> eventClass, Handler<E> handler) {
        List<Handler<?>> handlers = HANDLERS.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>());
        handlers.add(handler);
        if (REGISTERED.add(eventClass)) {
            KCommon.getInstance().getEntityStoreRegistry().registerSystem(new Dispatcher<>(eventClass));
        }
        return () -> handlers.remove(handler);
    }

    private static final class Dispatcher<E extends EcsEvent> extends EntityEventSystem<EntityStore, E> {
        private final Class<E> eventClass;

        private Dispatcher(Class<E> eventClass) {
            super(eventClass);
            this.eventClass = eventClass;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handle(int index, ArchetypeChunk<EntityStore> chunk, Store<EntityStore> store, CommandBuffer<EntityStore> buffer, E event) {
            List<Handler<?>> handlers = HANDLERS.get(eventClass);
            if (handlers == null || handlers.isEmpty())
                return;

            Ref<EntityStore> entity = chunk.getReferenceTo(index);
            for (Handler<?> handler : handlers) {
                try {
                    ((Handler<E>) handler).handle(entity, store, buffer, event);
                } catch (Throwable thr) {
                    HelperExceptions.reportEvent(event, thr);
                }
            }
        }

        @Override
        public Query<EntityStore> getQuery() {
            return Query.any();
        }
    }
}
