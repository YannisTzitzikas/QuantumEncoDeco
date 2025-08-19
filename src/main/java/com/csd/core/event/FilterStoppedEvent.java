package com.csd.core.event;

public class FilterStoppedEvent extends Event {
    private final String filterName;
    private final long durationNanos;

    public FilterStoppedEvent(String filterName, long durationNanos) {
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