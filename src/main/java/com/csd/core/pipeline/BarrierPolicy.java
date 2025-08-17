package com.csd.core.pipeline;

public class BarrierPolicy implements CoordinationPolicy {
    @Override
    public boolean shouldProcess(PortInbox inbox) {
        // Only process when every input has an EOS
        return inbox.allInputsEOS() && !inbox.allInputsEOSHead();
    }

    @Override
    public boolean shouldTerminate(PortInbox inbox) {
        return inbox.allInputsEOSHead();
    }
}
