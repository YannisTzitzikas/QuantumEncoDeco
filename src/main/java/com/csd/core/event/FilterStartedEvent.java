package com.csd.core.event;

public class FilterStartedEvent extends Event {
    private final String filterName;

    public FilterStartedEvent(String filterName) {
        this.filterName = filterName;
    }

    public String getFilterName() {
        return filterName;
    }
}