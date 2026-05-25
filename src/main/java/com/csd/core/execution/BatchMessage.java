package com.csd.core.execution;

import java.util.List;

public record BatchMessage<T>(List<T> payload, boolean isEos) {
    public static <T> BatchMessage<T> data(List<T> batch) {
        return new BatchMessage<>(batch, false);
    }

    public static <T> BatchMessage<T> eos() {
        return new BatchMessage<>(List.of(), true);
    }
}