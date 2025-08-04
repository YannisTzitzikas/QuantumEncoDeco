package com.csd.core.stats;

public class StatisticsCollector {
    private int totalTriples = 0;
    private int totalComponents = 0;
    private int uniquePredicates = 0;
    private int uniqueEntities = 0;
    private int uniqueComponents = 0;
    private int batchCount = 0;
    private int fileCount = 0;

    public void recordTriple() {
        totalTriples++;
        totalComponents += 3;
    }

    public void recordUniqueEntity() {
        uniqueEntities++;
        uniqueComponents++;
    }

    public void recordUniquePredicate() {
        uniquePredicates++;
        uniqueComponents++;
    }

    public void recordBatch() {
        batchCount++;
    }

    public void recordFile() {
        fileCount++;
    }

    public void printSummary() {
        System.out.println("=== Encoding Statistics ===");
        System.out.println("Total Triples: " + totalTriples);
        System.out.println("Total Components: " + totalComponents);
        System.out.println("Unique Entities (Objects + Subjects): " + uniqueEntities);
        System.out.println("Unique Predicates: " + uniquePredicates);
        System.out.println("Unique Components: " + uniqueComponents);
        System.out.println("Files Processed: " + fileCount);
        System.out.println("Batches Processed: " + batchCount);
    }

    // Getters
    public int getTotalTriples() {
        return totalTriples;
    }

    public int getTotalComponents() {
        return totalComponents;
    }

    public int getUniquePredicates() {
        return uniquePredicates;
    }

    public int getUniqueEntities() {
        return uniqueEntities;
    }

    public int getUniqueComponents() {
        return uniqueComponents;
    }

    public int getBatchCount() {
        return batchCount;
    }

    public int getFileCount() {
        return fileCount;
    }
}
