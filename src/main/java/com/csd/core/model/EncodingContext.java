package com.csd.core.model;

import java.util.Map;
import com.csd.core.storage.StorageEngine;

public class EncodingContext<T> {
    private final StorageEngine<T> storageEngine;
    private final Map<String, Object> parameters;

    private EncodingStatus status;

    public EncodingContext(StorageEngine<T> storageEngine,
            Map<String, Object> parameters) {
        this.storageEngine = storageEngine;
        this.parameters = parameters;
        this.status = EncodingStatus.NOT_STARTED;
    }

    public StorageEngine<T> getStorageEngine() {
        return storageEngine;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public Object getParameter(String key) {
        return parameters.get(key);
    }

    public <V> V getParameter(String key, Class<V> type) {
        return type.cast(parameters.get(key));
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
