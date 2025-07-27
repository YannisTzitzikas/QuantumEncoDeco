package com.csd.client.model;

public class JobStatus {
    private String jobId;
    private String state; // PENDING, PROCESSING, COMPLETED, FAILED
    private int progress; // 0-100
    private String details;
    
    // Getters and setters
    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}