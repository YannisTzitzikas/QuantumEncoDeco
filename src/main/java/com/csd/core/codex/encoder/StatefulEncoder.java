package com.csd.core.codex.encoder;

import com.csd.core.model.EncodingData;
import com.csd.core.storage.StorageEngine;
import com.csd.core.storage.StorageException;

public abstract class StatefulEncoder<T> implements IEncoder<T> {
    
    // Storage engine for stateful encoders
    protected final StorageEngine<T> engine;

    StatefulEncoder(StorageEngine<T> engine)
    {
        this.engine = engine;
    }
    
    @Override
    public T encode(EncodingData data) {
        
        T val = createEncoding(data);

        try {
            engine.put(data.getValue(), val);
        } catch (StorageException e) {
            e.printStackTrace();
        }

        return val;
    }
    
    protected abstract T createEncoding(EncodingData data);

    @Override
    public boolean isStateful(){
        return true;
    }
}
