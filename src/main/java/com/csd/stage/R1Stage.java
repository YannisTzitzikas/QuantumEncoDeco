package com.csd.stage;

import com.csd.core.stage.Stage;
import com.csd.core.stage.StageDescriptor;

@SuppressWarnings("unused")
public final class R1Stage implements Stage {
    private final int startOffset;
    private final StageDescriptor profile;

    public R1Stage(int startOffset, StageDescriptor profile) {
        this.startOffset = startOffset;
        this.profile = profile;
    }
}
