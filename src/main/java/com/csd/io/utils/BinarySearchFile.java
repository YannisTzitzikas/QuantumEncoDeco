package com.csd.io.utils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class BinarySearchFile {
    public static String search(File filePath, String targetWord) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            long left = 0;
            long right = file.length();

            while (left < right) {
                long mid = (left + right) / 2;
                file.seek(mid);

                if (mid != 0) file.readLine(); 

                String line = file.readLine();
                if (line == null) break;

                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                String term = parts[0].trim();
                int storedId = Integer.parseInt(parts[1].trim());

                int comparison = term.compareTo(targetWord);

                if (comparison == 0) {
                    return term + "," + storedId;
                } else if (comparison < 0) {
                    left = mid + 1;
                } else {
                    right = mid;
                }
            }
        }
        return null; // Term not found
    }
}
