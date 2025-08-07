package com.csd.core.stats;

public class StatisticsCollector {

    // Overall statistics
    private int     totalTriples = 0;
    private int     totalComponents = 0;
    private int     uniquePredicates = 0;
    private int     uniqueEntities = 0;
    private int     uniqueComponents = 0;
    private int     batchCount = 0;
    private int     fileCount = 0;

    // Per file statistics
    private int     triplesInCurrFile = 0;

    private int     maxTriplesPerFile = 0;
    private String  fileWithMostTriples = null;

    private int     minTriplesPerFile = Integer.MAX_VALUE;
    private String  fileWithLeastTriples = null;

    private long    minTime   = Long.MAX_VALUE;
    private long    maxTime   = 0;

    private long    totalTime = 0;
    private long    avgTime   = 0;

    public void recordTriple() {
        triplesInCurrFile++;
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

    public void recordFile(String fileName) {
        evaluateTriplesForFile(fileName);
        triplesInCurrFile = 0;
        fileCount++;
    }

    public void addTime(long ns) {
        minTime = Math.min(ns, minTime);
        maxTime = Math.max(ns, maxTime);
        totalTime += ns;

        avgTime = totalTime / fileCount ;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();

        builder.append("=== Encoding Statistics ===")
                .append("\nTotal Triples: " + totalTriples)
                .append("\nTotal Components: " + totalComponents)
                .append("\nUnique Entities (Objects + Subjects): " + uniqueEntities)
                .append("\nUnique Predicates: " + uniquePredicates)
                .append("\nUnique Components: " + uniqueComponents)
                .append("\nFiles Processed: " + fileCount)
                .append("\nBatches Processed: " + batchCount)
                .append("\nMax Triples per file: " + maxTriplesPerFile + " in " + fileWithMostTriples)
                .append("\nMin Triples per file: " + minTriplesPerFile + " in " + fileWithLeastTriples)
                .append("\n\n========================================")
                .append("\nAvg Time: ").append(formatMinutes(avgTime))
                .append("\nMin Time: ").append(formatMinutes(minTime))
                .append("\nMax Time: ").append(formatMinutes(maxTime))
                .append("\nTotal Time: ").append(formatMinutes(totalTime));
        
        return builder.toString();
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

    public int getMaxTriplesPerFile() {
         return maxTriplesPerFile;
    }
    
    public String getFileWithMostTriples() {
         return fileWithMostTriples;
    }
    
    public int getMinTriplesPerFile() {
         return minTriplesPerFile;
    }
    
    public String getFileWithLeastTriples() {
         return fileWithLeastTriples;
    }

    long getMinTime() {
        return minTime;    
    }

    long getMaxTime() {
        return maxTime;    
    }

    long getTotalTime() {
        return totalTime;  
    } 

    long getAvgTime()  {
        return avgTime;    
    } 


    // Utilities
    private void evaluateTriplesForFile(String fileName) {
        // Check for new minimum
        if (triplesInCurrFile < minTriplesPerFile) {
            minTriplesPerFile = triplesInCurrFile;
            fileWithLeastTriples = fileName;
        }

        // Check for new maximum
        if (triplesInCurrFile > maxTriplesPerFile) {
            maxTriplesPerFile = triplesInCurrFile;
            fileWithMostTriples = fileName;
        }
    }

    private String formatMinutes(long nanoseconds) {
        double minutes = nanoseconds / 1_000_000_000.0 / 60.0;
        return String.format("%.2f minutes", minutes);
    }

}
