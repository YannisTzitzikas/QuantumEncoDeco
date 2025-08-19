package com.csd.core.event;

public class FilterLoopStoppedEvent extends Event {
    private final String filterName;
    private final long durationNanos;

    public FilterLoopStoppedEvent(String filterName, long durationNanos) {
        this.filterName = filterName;
        this.durationNanos = durationNanos;
    }

    public String getFilterName() {
        return filterName;
    }

    public long getDurationNanos() {
        return durationNanos;
    }
}