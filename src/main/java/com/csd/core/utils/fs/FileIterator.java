package com.csd.core.utils.fs;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class FileIterator implements Iterator<Path> {
    private final Queue<Path> fileQueue;

    public FileIterator(Path path, String globPattern) throws IOException {
        this.fileQueue = new ArrayDeque<>();

        if (Files.isRegularFile(path)) {
            // If it's a single file, add it directly (ignore pattern)
            fileQueue.offer(path);
        } else if (Files.isDirectory(path)) {
            PathMatcher matcher = (globPattern != null && !globPattern.equals("*"))
                    ? FileSystems.getDefault().getPathMatcher("glob:" + globPattern)
                    : null;
            collectFiles(path, fileQueue, matcher);
        } else {
            throw new IllegalArgumentException("Provided path is neither a file nor a directory!");
        }
    }

    private void collectFiles(Path directory, Queue<Path> fileQueue, PathMatcher matcher) throws IOException {
        Files.walk(directory)
                .filter(Files::isRegularFile)
                .filter(p -> matcher == null || matcher.matches(p.getFileName()))
                .forEach(fileQueue::offer);
    }

    @Override
    public boolean hasNext() {
        return !fileQueue.isEmpty();
    }

    @Override
    public Path next() {
        return fileQueue.poll();
    }
}
