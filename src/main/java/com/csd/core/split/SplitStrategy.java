package com.csd.core.split;

import java.util.List;

public interface SplitStrategy {
    List<SplitPart> split(Object input);

    final class SplitPart {
        private final String portName;
        private final Object payload;

        public SplitPart(String portName, Object payload) {
            this.portName = portName;
            this.payload = payload;
        }
        public String getPortName() { return portName; }
        public Object getPayload() { return payload; }
        @Override public String toString() { return portName + " -> " + payload; }
    }
}
