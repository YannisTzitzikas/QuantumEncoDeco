package com.csd.stage;

@SuppressWarnings("unused")
public final class R1Stage implements Stage {
    private final int startOffset;
    private final StageDescriptor profile;

    public R1Stage(int startOffset, StageDescriptor profile) {
        this.startOffset = startOffset;
        this.profile = profile;
    }
}
