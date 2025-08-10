package com.csd.stage;

public final class R1Stage implements Stage {
    private final int startOffset;
    private final StageProfile profile;

    public R1Stage(int startOffset, StageProfile profile) {
        this.startOffset = startOffset;
        this.profile = profile;
    }
}
