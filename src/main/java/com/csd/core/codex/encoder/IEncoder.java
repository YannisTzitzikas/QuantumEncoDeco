package com.csd.core.codex.encoder;

import com.csd.core.model.EncoderInfo;
import com.csd.core.model.EncodingContext;
import com.csd.core.model.EncodingData;

public interface IEncoder<T> {

    // TODO(gtheo): Check wether the getFinalEncoding return type causes issues
    T                   encode(EncodingData data);
    String              getFinalEncoding(EncodingData data);

    void                setContext(EncodingContext<T> context);  // Set context explicitly
    EncodingContext<T>  getContext();              // Retrieve context
    EncoderInfo         getInfo();

}
