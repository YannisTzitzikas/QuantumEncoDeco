package com.ics.io.utils;

import java.io.IOException;
import java.io.RandomAccessFile;

public class BinarySearchFile {
    public static String binarySearchWord(String filePath, String targetWord) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long low = 0;
            long high = file.length();
            
            while (low <= high) {
                long mid = (low + high) / 2;

                file.seek(mid);
                file.readLine();
                long pos = file.getFilePointer();

                String line = file.readLine();
                if (line == null) return null;

                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue; 
                String term = parts[0];
                String id = parts[1];

                int comparison = term.compareTo(targetWord);

                if (comparison == 0) {
                    return term + " " + id;
                } else if (comparison < 0) {
                    low = pos; 
                } else {
                    high = mid - 1; 
                }
            }
        }
        return null; // Term not found
    }
}
