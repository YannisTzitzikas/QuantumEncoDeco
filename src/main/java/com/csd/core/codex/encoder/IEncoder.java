package com.csd.core.codex.encoder;

import com.csd.core.model.EncoderInfo;
import com.csd.core.model.JobContext;
import com.csd.core.model.EncodingData;

public interface IEncoder<T> {

    T                   encode(EncodingData data);
    String              getFinalEncoding(EncodingData data);

    void                setContext(JobContext context);  // Set context explicitly
    JobContext     getContext();              
    EncoderInfo         getInfo();

}
