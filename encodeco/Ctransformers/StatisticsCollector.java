package Ctransformers;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class StatisticsCollector {
    // Triple counts
    private final AtomicInteger totalTriples = new AtomicInteger(0);
    
    // Component tracking (storing references to canonical URIs)
    private final Set<String> subjects = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> predicates = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final Set<String> objects = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    // Bit metrics (computed later)
    private int bitsPerEntity;
    private int bitsPerPredicate;
    
    // Counting distincts using set sizes
    private final Function<Set<String>, Integer> distinctCounter = Set::size;
    
    public void recordTriple() {
        totalTriples.incrementAndGet();
    }
    
    public void recordComponent(String uri, TripleComponent component) {
        String canonical = URIFactory.getURI(uri);
        switch (component) {
            case SUBJECT: subjects.add(canonical); break;
            case PREDICATE: predicates.add(canonical); break;
            case OBJECT: objects.add(canonical); break;
        }
    }

    // Getters (compute on demand)
    public int getTotalTriples() { return totalTriples.get(); }
    public int getDistinctSubjects() { return distinctCounter.apply(subjects); }
    public int getDistinctPredicates() { return distinctCounter.apply(predicates); }
    public int getDistinctObjects() { return distinctCounter.apply(objects); }
    public int getDistinctEntities() {
        return (int) Stream.concat(subjects.stream(), objects.stream())
                          .distinct()
                          .count();
    }
    
    // Bit calculation methods
    public void setBitsPerEntity(int bits) { this.bitsPerEntity = bits; }
    public void setBitsPerPredicate(int bits) { this.bitsPerPredicate = bits; }
    public int getBitsPerTriple() { return 2 * bitsPerEntity + bitsPerPredicate; }
    public long getTotalBits() { return (long) totalTriples.get() * getBitsPerTriple(); }
    
    // Optimized memory report
    @Override
    public String toString() {
        return String.format(
            "RDF Statistics:\n" +
            "===============\n" +
            "Total Triples: %,d\n" +
            "Distinct Subjects: %,d\n" +
            "Distinct Predicates: %,d\n" +
            "Distinct Objects: %,d\n" +
            "Distinct Entities: %,d\n\n" +
            
            "Encoding Statistics:\n" +
            "====================\n" +
            "Bits per entity: %,d\n" +
            "Bits per predicate: %,d\n" +
            "Bits per triple: %,d\n" +
            "Total bits: %,d (%,.4f MB)",
            getTotalTriples(),
            getDistinctSubjects(),
            getDistinctPredicates(),
            getDistinctObjects(),
            getDistinctEntities(),
            bitsPerEntity,
            bitsPerPredicate,
            getBitsPerTriple(),
            getTotalBits(),
            getTotalBits() / (1024.0 * 1024.0 * 8)
        );
    }
    
    // Memory-efficient reset for pipeline reuse
    public void reset() {
        totalTriples.set(0);
        subjects.clear();
        predicates.clear();
        objects.clear();
    }
}