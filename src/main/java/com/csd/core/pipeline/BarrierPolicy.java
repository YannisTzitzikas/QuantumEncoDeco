package com.csd.core.pipeline;

public class BarrierPolicy implements CoordinationPolicy {
    @Override
    public boolean shouldProcess(PortInbox inbox) {
        // Only process when every input's head is EOS
        return inbox.allInputsEOS();
    }

    @Override
    public boolean shouldTerminate(PortInbox inbox) {
        // Terminate immediately after processing
        return inbox.allInputsEOS();
    }
}
