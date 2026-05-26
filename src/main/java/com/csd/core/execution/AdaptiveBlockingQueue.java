package com.csd.core.execution;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An unbounded backing queue wrapped with a dynamic TCP-style congestion control window.
 * Safely handles virtual thread execution channels by scaling limits up and down.
 */
public class AdaptiveBlockingQueue<E> extends LinkedBlockingQueue<E> {
    private final AtomicInteger currentWindowLimit;
    private final int minWindow;
    private final int maxWindow;

    public AdaptiveBlockingQueue(int initialWindow, int minWindow, int maxWindow) {
        super(); // Unbounded capacity internally so super.put() never deadlocks on its own static lock
        this.currentWindowLimit = new AtomicInteger(initialWindow);
        this.minWindow = minWindow;
        this.maxWindow = maxWindow;
    }

    /**
     * Returns the current dynamic sliding window capacity.
     */
    public int getCurrentWindowLimit() {
        return currentWindowLimit.get();
    }

    /**
     * Returns the current saturation level of the queue.
     * @return a ratio between 0.0 (empty) and 1.0 (at or exceeding window limit).
     */
    public double getFillRatio() {
        int limit = currentWindowLimit.get();
        if (limit == 0) return 1.0;
        return (double) size() / limit;
    }

    @Override
    public int remainingCapacity() {
        return Math.max(0, currentWindowLimit.get() - size());
    }

    @Override
    public void put(E e) throws InterruptedException {
        synchronized (this) {
            // Block the thread if we have filled our current dynamic sliding window
            while (size() >= currentWindowLimit.get()) {
                this.wait();
            }
        }
        super.put(e);
    }

    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        E item = super.poll(timeout, unit);
        if (item == null) return null;

        // --- TCP AIMD Congestion Control Evaluation ---
        int currentSize = size();
        int oldLimit = currentWindowLimit.get();

        if (currentSize > (oldLimit * 0.75)) {
            // Shrink window aggressively to limit memory usage and push backpressure upstream.
            int newLimit = Math.max(minWindow, (int) (oldLimit * 0.70));
            currentWindowLimit.set(newLimit);
        } else if (currentSize < (oldLimit * 0.25)) {
            // Slowly open up the window capacity to permit higher throughput bursts.
            int newLimit = Math.min(maxWindow, oldLimit + 2);
            currentWindowLimit.set(newLimit);
        }

        // Always notify any waiting producers since queue size dropped
        synchronized (this) {
            this.notifyAll();
        }
        return item;
    }
}