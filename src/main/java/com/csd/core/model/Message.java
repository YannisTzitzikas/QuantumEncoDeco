package com.csd.core.model;

import java.util.Objects;

public final class Message<T> {
    private final MessageKind kind;
    private final T payload;
    private final String edgeId;

    private Message(MessageKind kind, T payload, String edgeId) {
        this.kind = kind;
        this.payload = payload;
        this.edgeId = edgeId;
    }

    public static <T> Message<T> data(T payload, String edgeId) {
        return new Message<>(MessageKind.DATA, payload, edgeId);
    }

    public static <T> Message<T> eos(String edgeId) {
        return new Message<>(MessageKind.EOS, null, edgeId);
    }

    public static <T> Message<T> control(T payload, String edgeId) {
        return new Message<>(MessageKind.CONTROL, payload, edgeId);
    }

    public enum MessageKind { DATA, EOS, CONTROL }

    // Getters
    public MessageKind getKind()    { return kind; }
    public T           getPayload() { return payload; }
    public String      getEdgeId()  { return edgeId; }

    @Override
    public String toString() {
        return "Message{" + kind + ", edge=" + edgeId + ", payload=" + Objects.toString(payload) + "}";
    }

}