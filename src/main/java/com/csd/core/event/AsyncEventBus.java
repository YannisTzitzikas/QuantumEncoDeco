package com.csd.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class AsyncEventBus implements EventBus {
    private static final Logger logger = LoggerFactory.getLogger(AsyncEventBus.class);
    private final Executor executor;
    private final ConcurrentMap<Class<? extends Event>, CopyOnWriteArrayList<Consumer<? extends Event>>> subscribers;

    public AsyncEventBus(Executor executor) {
        this.executor = executor;
        this.subscribers = new ConcurrentHashMap<>();
    }

    @Override
    public <E extends Event> void subscribe(Class<E> eventType, Consumer<E> handler) {
        subscribers.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                   .add(handler);
    }

    @Override
    public void unsubscribe(Class<? extends Event> eventType, Consumer<? extends Event> handler) {
        CopyOnWriteArrayList<Consumer<? extends Event>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            handlers.remove(handler);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(Event event) {
        Class<? extends Event> eventType = event.getClass();
        CopyOnWriteArrayList<Consumer<? extends Event>> handlers = subscribers.get(eventType);
        if (handlers != null) {
            for (Consumer<? extends Event> handler : handlers) {
                executor.execute(() -> {
                    try {
                        ((Consumer<Event>) handler).accept(event);
                    } catch (Exception e) {
                        logger.error("Error handling event {}", eventType.getSimpleName(), e);
                    }
                });
            }
        }
    }
}