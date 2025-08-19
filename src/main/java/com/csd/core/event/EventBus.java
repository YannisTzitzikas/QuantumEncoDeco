package com.csd.core.event;

import java.util.function.Consumer;

public interface EventBus {
    <E extends Event> void subscribe(Class<E> eventType, Consumer<E> handler);
    void unsubscribe(Class<? extends Event> eventType, Consumer<? extends Event> handler);
    void publish(Event event);
}