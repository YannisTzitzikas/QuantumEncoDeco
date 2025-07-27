package com.csd.core.codex.encoder;

import java.util.Map;

import com.csd.core.model.EncoderSettings;
import com.csd.core.model.EncodingData;

public interface IEncoder<T> {
    T               encode(EncodingData data);
    void            acceptParams(Map<String, Object> params);
    EncoderSettings getSettings();
}
