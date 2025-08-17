package com.csd.core.pipeline;

import com.csd.core.model.Message;

import java.util.Deque;
import java.util.Map;

public final class PortInbox {
    // Per-input-port unconsumed messages
    private final Map<String, Deque<Message<?>>> buffers;

    public PortInbox(Map<String, Deque<Message<?>>> buffers) {
        this.buffers = buffers;
    }

    public Map<String, Deque<Message<?>>> buffers() { return buffers; }

    public boolean allInputsEOSHead() {
        return buffers.values().stream().allMatch(q -> !q.isEmpty() && q.peek().getKind() == Message.MessageKind.EOS);
    }

    public boolean allInputsEOS() {
        return buffers.values().stream().allMatch(q -> !q.isEmpty() && q.peekLast().getKind() == Message.MessageKind.EOS);
    }

    public boolean anyInputHasData() {
        return buffers.values().stream().anyMatch(q -> !q.isEmpty() && q.peek().getKind() == Message.MessageKind.DATA);
    }

    public boolean allInputsHaveAtLeastOne() {
        return buffers.values().stream().allMatch(q -> !q.isEmpty());
    }
}