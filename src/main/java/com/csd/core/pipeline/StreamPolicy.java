package com.csd.core.pipeline;

public final class StreamPolicy implements CoordinationPolicy {
    @Override public boolean shouldProcess(PortInbox inbox) {
        return inbox.anyInputHasData();
    }
    @Override public boolean shouldTerminate(PortInbox inbox) {
        return inbox.allInputsEOS();
    }
}