package com.csd.core.codex.encoder;

import com.csd.core.model.EncodingData;

public interface IEncoder<T> {
    T encode(EncodingData data);
    boolean isStateful();
}
