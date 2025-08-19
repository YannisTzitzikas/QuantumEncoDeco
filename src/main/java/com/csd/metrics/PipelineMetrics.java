package com.csd.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csd.core.event.EventBus;
import com.csd.core.event.FilterLoopStartedEvent;
import com.csd.core.event.FilterLoopStoppedEvent;
import com.csd.core.event.FilterStartedEvent;
import com.csd.core.event.FilterStoppedEvent;

public class PipelineMetrics {
    private static final Logger logger = LoggerFactory.getLogger(PipelineMetrics.class);
    private final Map<String, FilterMetrics> metricsMap = new ConcurrentHashMap<>();

    public PipelineMetrics(EventBus bus)
    {
        if(bus != null) {
            bus.subscribe(FilterStartedEvent.class , e -> handleStarted(e));
            bus.subscribe(FilterLoopStartedEvent.class, e -> handleLoopStarted(e));
            bus.subscribe(FilterStoppedEvent.class , e -> handleStopped(e));
            bus.subscribe(FilterLoopStoppedEvent.class, e -> handleLoopStopped(e));
        }
    }

    private void handleStarted(FilterStartedEvent event) {
        String name = event.getFilterName();
        metricsMap.putIfAbsent(name, new FilterMetrics(name));
        logger.info("Filter started: {}", name);
    }

    private void handleStopped(FilterStoppedEvent event) {
        String name = event.getFilterName();
        FilterMetrics metrics = metricsMap.get(name);
        if (metrics != null) {
            metrics.setTotalRuntime(event.getDurationNanos());
            logger.info("Filter stopped: {} | Total runtime: {} ns", name, event.getDurationNanos());
        }
    }

    private void handleLoopStarted(FilterLoopStartedEvent event) {
        // Optional: could track loop start timestamps if needed
        logger.debug("Loop started for filter: {}", event.getFilterName());
    }

    private void handleLoopStopped(FilterLoopStoppedEvent event) {
        String name = event.getFilterName();
        FilterMetrics metrics = metricsMap.get(name);
        if (metrics != null) {
            metrics.incrementLoopCount();
            metrics.addLoopDuration(event.getDurationNanos());
            logger.debug("Loop stopped for filter: {} | Duration: {} ns", name, event.getDurationNanos());
        }
    }

    public FilterMetrics getMetrics(String filterName) {
        return metricsMap.get(filterName);
    }

    public Map<String, FilterMetrics> getAllMetrics() {
        return metricsMap;
    }

    public static class FilterMetrics {
        private final String filterName;
        private final AtomicLong loopCount = new AtomicLong(0);
        private final AtomicLong totalLoopDuration = new AtomicLong(0);
        private volatile long totalRuntimeNanos = 0;

        public FilterMetrics(String filterName) {
            this.filterName = filterName;
        }

        public void incrementLoopCount() {
            loopCount.incrementAndGet();
        }

        public void addLoopDuration(long duration) {
            totalLoopDuration.addAndGet(duration);
        }

        public void setTotalRuntime(long duration) {
            this.totalRuntimeNanos = duration;
        }

        public String getFilterName() {
            return filterName;
        }

        public long getLoopCount() {
            return loopCount.get();
        }

        public long getTotalLoopDuration() {
            return totalLoopDuration.get();
        }

        public long getTotalRuntimeNanos() {
            return totalRuntimeNanos;
        }

        @Override
        public String toString() {
            return String.format("FilterMetrics{name='%s', loops=%d, loopTime=%dns, totalTime=%dns}",
                    filterName, getLoopCount(), getTotalLoopDuration(), getTotalRuntimeNanos());
        }
    }
}
