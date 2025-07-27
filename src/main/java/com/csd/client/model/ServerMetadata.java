package com.csd.client.model;

import java.util.List;

public class ServerMetadata {
    private String serverVersion;
    private List<String> supportedEncodings;
    private int activeJobs;
    private String lastUpdated;
    
    // Getters and setters
    public String getServerVersion() { return serverVersion; }
    public void setServerVersion(String serverVersion) { this.serverVersion = serverVersion; }
    public List<String> getSupportedEncodings() { return supportedEncodings; }
    public void setSupportedEncodings(List<String> supportedEncodings) { this.supportedEncodings = supportedEncodings; }
    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
}