package com.csd.core.event;

public class FilterLoopStartedEvent extends Event {
    private final String filterName;

    public FilterLoopStartedEvent(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterName() {
        return filterName;
    }
}