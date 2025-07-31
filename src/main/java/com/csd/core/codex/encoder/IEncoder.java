package com.csd.core.codex.encoder;

import com.csd.core.model.EncoderInfo;
import com.csd.core.model.EncodingContext;
import com.csd.core.model.EncodingData;

public interface IEncoder<T> {

    T                   encode(EncodingData data);
    String              getFinalEncoding(EncodingData data);

    void                setContext(EncodingContext context);  // Set context explicitly
    EncodingContext     getContext();              
    EncoderInfo         getInfo();

}
