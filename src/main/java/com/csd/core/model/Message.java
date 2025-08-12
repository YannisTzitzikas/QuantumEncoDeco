package com.csd.core.model;

import java.util.Objects;

public final class Message {
    private final MessageKind kind;
    private final Object payload;

    private Message(MessageKind kind, Object payload) {
        this.kind = kind;
        this.payload = payload;
    }

    public static  Message data(Object payload) {
        return new Message(MessageKind.DATA, payload);
    }

    public static  Message eos(String edgeId) {
        return new Message(MessageKind.EOS, null);
    }

    public static  Message control(Object payload) {
        return new Message(MessageKind.CONTROL, payload);
    }

    public enum MessageKind { DATA, EOS, CONTROL }

    // Getters
    public MessageKind      getKind()    { return kind; }
    public Object           getPayload() { return payload; }

    @Override
    public String toString() {
        return "Message{" + kind + ", payload=" + Objects.toString(payload) + "}";
    }

}