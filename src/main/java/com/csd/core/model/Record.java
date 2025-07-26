package com.csd.core.model;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

// TODO(gtheo): Think of a better abstraction and better names
public class Record implements Iterable<String>{

    private int          maxSize = Integer.MAX_VALUE; 
    private List<String> elements;

    public Record(String... elements) {
        this.elements = Arrays.asList(elements);
    }

    public List<String> getElements() {
        return elements;
    }

    public void add(String element) {
        elements.add(element);
    }

    public int size() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public void limitSize(int maxSize) {
        if (maxSize < 0) {
            throw new IllegalArgumentException("Max size cannot be negative.");
        }
        this.maxSize = maxSize;
        while (elements.size() > maxSize) {
            elements.remove(elements.size() - 1); // Trim excess elements
        }
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void clear() {
        elements.clear();
    }

    @Override
    public String toString() {
        return elements.toString();
    }

    @Override
    public Iterator<String> iterator() {
        return elements.iterator(); 
    }
}
