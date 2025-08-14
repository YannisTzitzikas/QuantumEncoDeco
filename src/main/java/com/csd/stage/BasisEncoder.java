package com.csd.stage;

import com.csd.core.stage.Stage;
import com.csd.core.stage.StageDescriptor;

@SuppressWarnings("unused")
public final class BasisEncoder implements Stage {
    private final int startOffset;
    private final StageDescriptor profile;

    public BasisEncoder(int startOffset, StageDescriptor profile) {
        this.startOffset = startOffset;
        this.profile = profile;
    }
}
