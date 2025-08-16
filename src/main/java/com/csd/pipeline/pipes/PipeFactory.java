package com.csd.pipeline.pipes;

import com.csd.core.pipeline.Pipe;

public final class PipeFactory {

    private PipeFactory() {} // Prevent instantiation

    /**
     * Creates a new blocking pipe for the specified message type.
     * 
     * @param <T>   The type of payload in the Message
     * @param clazz The class token for type safety (used for type capture)
     * @return A new Pipe instance configured for the specified message type
     */
    public static <T> Pipe<T> createPipe(Class<T> clazz, int capacity) {
        return new BlockingQueuePipe<>(capacity);
    }
}
