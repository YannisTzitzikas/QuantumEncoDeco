package com.csd.core.model;

public class URITriple {
    private final String subject;
    private final String predicate;
    private final String object;
    
    public URITriple(String subject, String predicate, String object) {
        this.subject = validate(subject, "subject");
        this.predicate = validate(predicate, "predicate");
        this.object = validate(object, "object");
    }
    
    private String validate(String value, String role) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(role + " cannot be null or blank");
        }
        return value;
    }
    
    // Network-friendly serialization
    public String toWireFormat() {
        return subject + "\u0001" + predicate + "\u0001" + object;
    }
    
    public static URITriple fromWireFormat(String wire) {
        String[] parts = wire.split("\u0001", 3);
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid wire format");
        }
        return new URITriple(parts[0], parts[1], parts[2]);
    }
    
    // Getters
    public String getSubject() { return subject; }
    public String getPredicate() { return predicate; }
    public String getObject() { return object; }
    
    @Override
    public String toString() {
        return "(" + subject + ", " + predicate + ", " + object + ")";
    }
}