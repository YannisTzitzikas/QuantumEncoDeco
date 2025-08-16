package com.csd.core.pipeline;

/**
 * Join semantics: wait until all inputs have a message.
 * If some inputs present EOS among others’ DATA, process DATA and ignore those EOS (they’ll be consumed later).
 * Terminate only when every input has produced EOS and no more DATA can be made.
 */
public final class JoinPolicy implements CoordinationPolicy {
    @Override public boolean shouldProcess(PortInbox inbox) {
        // Only process when every input has at least one message, and at least one is DATA.
        if (!inbox.allInputsHaveAtLeastOne()) return false;
        return inbox.anyInputHasData();
    }

    @Override public boolean shouldTerminate(PortInbox inbox) {
        return inbox.allInputsEOS();
    }
}