package com.csd.metrics;

import java.util.concurrent.atomic.AtomicLong;

import com.csd.core.event.EventBus;
import com.csd.events.TripleProcessedEvent;
import com.csd.events.UniqueEntityEvent;
import com.csd.events.UniquePredicateEvent;

public class UriTripleMetrics {
    private final AtomicLong totalTriplesProcessed = new AtomicLong(0);
    private final AtomicLong totalEntitiesProcessed = new AtomicLong(0);
    private final AtomicLong totalPredicatesProcessed = new AtomicLong(0);
    private final AtomicLong uniqueEntitiesCount   = new AtomicLong(0);
    private final AtomicLong uniquePredicatesCount = new AtomicLong(0);
    
    public UriTripleMetrics(EventBus eventBus) {
        if (eventBus != null) {
            registerEventHandlers(eventBus);
        }
    }
    
    private void registerEventHandlers(EventBus eventBus) {
        eventBus.subscribe(TripleProcessedEvent.class, e -> {totalTriplesProcessed.incrementAndGet(); totalEntitiesProcessed.addAndGet(2); totalPredicatesProcessed.addAndGet(1); });
        eventBus.subscribe(UniqueEntityEvent.class, e -> uniqueEntitiesCount.getAndIncrement());
        eventBus.subscribe(UniquePredicateEvent.class, e -> uniquePredicatesCount.getAndIncrement());
    }
    
    // Getters
    public long getTotalTriplesProcessed() { return totalTriplesProcessed.get(); }
    public long getTotalEntitiesProcessed() { return totalEntitiesProcessed.get(); }
    public long getTotalPredicatesProcessed() { return totalPredicatesProcessed.get(); }
    public long getUniqueEntitiesCount() { return uniqueEntitiesCount.get(); }
    public long getUniquePredicatesCount() { return uniquePredicatesCount.get(); }
    public long getUniqueComponentsCount() { return uniqueEntitiesCount.get() + uniquePredicatesCount.get(); }
    public long getTotalComponentsProcessed() { return totalEntitiesProcessed.get() + totalPredicatesProcessed.get(); }
    
    public double getEntityUniquenessRatio() {
        return totalEntitiesProcessed.get() > 0 ? 
            (uniqueEntitiesCount.get() * 100.0) / totalEntitiesProcessed.get() : 0;
    }
    
    public double getPredicateUniquenessRatio() {
        return totalPredicatesProcessed.get() > 0 ? 
            (uniquePredicatesCount.get() * 100.0) / totalPredicatesProcessed.get() : 0;
    }

    public double getComponentUniquenessRatio() {
        return (totalPredicatesProcessed.get() + uniqueEntitiesCount.get()) > 0 ? 
            ((uniquePredicatesCount.get() + uniqueEntitiesCount.get()) * 100.0) / (totalPredicatesProcessed.get() + uniqueEntitiesCount.get()) : 0;
    }
}