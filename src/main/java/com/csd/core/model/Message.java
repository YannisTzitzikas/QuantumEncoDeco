package com.csd.core.model;

import java.util.Objects;

public final class Message<T> {
    private final MessageKind kind;
    private final T payload;

    private Message(MessageKind kind, T payload) {
        this.kind = Objects.requireNonNull(kind, "kind");
        this.payload = payload;
    }

    // ----- Factory methods -----

    public static <T> Message<T> data(T payload) {
        return new Message<>(MessageKind.DATA, payload);
    }

    public static Message<Void> eos(String edgeId) {
        // if you want to carry edgeId, change payload type accordingly
        return new Message<>(MessageKind.EOS, null);
    }

    public static <T> Message<T> control(T payload) {
        return new Message<>(MessageKind.CONTROL, payload);
    }

    // ----- Accessors -----
    public MessageKind getKind() {
        return kind;
    }

    public T getPayload() {
        return payload;
    }

    @Override
    public String toString() {
        return "Message{" + kind + ", payload=" + Objects.toString(payload) + "}";
    }

    public enum MessageKind { DATA, EOS, CONTROL }
}
