package com.csd.core.model;

import java.util.Map;

import com.csd.common.metrics.StatisticsCollector;
import com.csd.core.storage.StorageEngine;

@Deprecated
public class JobContext {

    private final JobType jobType;
    private final StorageEngine storageEngine;
    private final Map<String, Object> parameters;
    private final StatisticsCollector statCollector;

    private JobStatus status;

    public JobContext(JobType jobType,
            StorageEngine storageEngine,
            Map<String, Object> parameters,
            StatisticsCollector statCollector) {
        this.jobType = jobType;
        this.storageEngine = storageEngine;
        this.parameters = parameters;
        this.statCollector = statCollector;
        this.status = JobStatus.NOT_STARTED;
    }

    public JobType getJobType() {
        return jobType;
    }

    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    public StatisticsCollector getStatisticsCollector() {
        return statCollector;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public JobStatus getStatus() {
        return this.status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    public enum JobStatus {
        NOT_STARTED, RUNNING, DONE, FAILED
    }

    public enum JobType {
        ENCODING,
        DECODING
    }

}
