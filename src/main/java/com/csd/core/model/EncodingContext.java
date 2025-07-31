package com.csd.core.model;

import java.util.Map;
import com.csd.core.storage.StorageEngine;

public class EncodingContext {
    private final StorageEngine storageEngine;
    private final Map<String, Object> parameters;

    private EncodingStatus status;

    public EncodingContext(StorageEngine storageEngine,
            Map<String, Object> parameters) {
        this.storageEngine = storageEngine;
        this.parameters = parameters;
        this.status = EncodingStatus.NOT_STARTED;
    }

    public StorageEngine getStorageEngine() {
        return storageEngine;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public EncodingStatus getStatus() {
        return this.status;
    }

    public void setStatus(EncodingStatus status) {
        this.status = status;
    }

    public enum EncodingStatus {
        NOT_STARTED, RUNNING, DONE, FAILED
    }

}
