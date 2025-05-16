package com.ics.codec.encoding;

import com.ics.codec.EncodingType;

public class EncoderFactory {

    public static Encoder getEncoder(String encodingName) {
        EncodingType encodingType = EncodingType.fromString(encodingName);
        switch (encodingType) {
            case R1: return new R1Encoder();
            case R2: throw new UnsupportedOperationException("R2 Encoder not implemented yet.");
            default:
                throw new IllegalArgumentException("Unknown encoding type: " + encodingType);
        }
    }
}
