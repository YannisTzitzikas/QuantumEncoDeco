package com.csd.client.model;

public class JobResult {
    private String jobId;
    private String status;
    private String outputPath;
    private String errorMessage;
    
    // Getters and setters
    public String getJobId()                         { return jobId; }
    public String getStatus()                        { return status; }
    public String getErrorMessage()                  { return errorMessage; }
    public String getOutputPath()                    { return outputPath; }

    public void setJobId(String jobId)               { this.jobId = jobId; }
    public void setOutputPath(String outputPath)     { this.outputPath = outputPath; }
    public void setStatus(String status)             { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}