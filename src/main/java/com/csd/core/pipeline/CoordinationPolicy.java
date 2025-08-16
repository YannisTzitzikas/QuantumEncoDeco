package com.csd.core.pipeline;

public interface CoordinationPolicy {
    boolean shouldProcess(PortInbox inbox);
    boolean shouldTerminate(PortInbox inbox);
}
