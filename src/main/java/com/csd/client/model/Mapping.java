package com.csd.client.model;

public class Mapping {
    private String mappingId;
    private byte[] data;
    
    public Mapping(String mappingId, byte[] data) {
        this.mappingId = mappingId;
        this.data = data;
    }
    
    // Getters
    public String getMappingId() { return mappingId; }
    public byte[] getData() { return data; }
}